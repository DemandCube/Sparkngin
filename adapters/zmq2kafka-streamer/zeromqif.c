/*
 * Copyright (c) 2013, Steve Morin <steve@demandcube.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * ZeroMQ interface
 */


#include "zmq2kafka-streamer.h"

#include <pthread.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>


/* ZeroMQ context */
static void *zif_ctx;
/* ZeroMQ socket */
static void *zif_s;
/* ZeroMQ receiver thread */
static pthread_t zif_thread;


/**
 * ZeroMQ receiver thread main loop.
 */
static void *zif_main (void *arg) {

	while (conf.run) {
		zmq_msg_t *msg;
		int r;

		/* Allocate message handle */
		msg = malloc(sizeof(*msg));
		zmq_msg_init(msg);

		/* Receive message from ZeroMQ */
		r = zmq_recvmsg(zif_s, msg, 0);
		if (r == -1) {
			if (errno != EAGAIN) {
				ks_log(LOG_ERR, "zmq_recv failed: %s",
				       strerror(errno));
				sleep(1);
			}
			zmq_msg_close(msg);
			free(msg);
			continue;
		}

		/* Hand message over to Kafka.
		 * A callback will be called when the message is delivered
		 * and the callback will free the message. */
		if (rd_kafka_produce(conf.rkt, conf.kafka_partition, 0,
				     zmq_msg_data(msg),
				     zmq_msg_size(msg),
				     NULL, 0,
				     msg) == -1) {
			ks_log(LOG_WARNING,
			       "Kafka produce failed: %s", strerror(errno));
			zmq_msg_close(msg);
			free(msg);
		}
	}


	return NULL;
}



/**
 * Stop ZeroMQ interface
 */
void zif_stop (void) {
	void *ret;

	if (zif_s) {
		/* Wait for zif thread to finish */
		pthread_join(zif_thread, &ret);

		zmq_close(zif_s);
	}

	if (zif_ctx)
		zmq_term(zif_ctx);
}


/**
 * Start ZeroMQ interface
 */
int zif_start (char *errstr, size_t errstr_size) {
	int i;
	int recv_timeout = 1000;
	
	/* Config validation */
	if (!conf.zmq_bind && !conf.zmq_connect) {
		snprintf(errstr, errstr_size,
			 "One of zeromq.bind or zeromq.connect must "
			 "be configured");
		return -1;
	}

	/* Create zeromq context */
	if (!(zif_ctx = zmq_init(4))) {
		snprintf(errstr, errstr_size,
			 "Failed to create zmq context: %s", strerror(errno));
		return -1;
	}

	/* Create zeromq socket */
	if (!(zif_s = zmq_socket(zif_ctx, conf.zmq_socket_type))) {
		snprintf(errstr, errstr_size,
			 "Failed to create zmq socket (type %i): %s",
			 conf.zmq_socket_type, strerror(errno));
		goto err;
	}

	/* Set recv timeout to not hang forever when shutting down. */
	zmq_setsockopt(zif_s, ZMQ_RCVTIMEO,
		       &recv_timeout, sizeof(recv_timeout));

	/* Set zeromq endpoint identity, if configured */
	if (conf.zmq_id) {
		if (zmq_setsockopt(zif_s, ZMQ_IDENTITY,
				   conf.zmq_id, strlen(conf.zmq_id)) == -1) {
			snprintf(errstr, errstr_size,
				 "Failed to set zmq identity to \"%s\": %s",
				 conf.zmq_id, strerror(errno));
			goto err;
		}
	}

	/* Configure socket according to its type */
	switch (conf.zmq_socket_type)
	{
	case ZMQ_SUB:
		/* Subscribe socket */
		ks_log(LOG_DEBUG, "Using ZMQ_SUB socket");

		if (!conf.zmq_subscriptions_cnt) {
			snprintf(errstr, errstr_size,
				 "No subscriptions configured for zmq "
				 "(zeromq.subscriptions=...)");
			goto err;
		}

		/* Add all configured subscriptions */
		for (i = 0 ; i < conf.zmq_subscriptions_cnt ; i++) {
			ks_log(LOG_DEBUG,
			       "zmq subscribe to \"%s\"",
			       conf.zmq_subscriptions[i]);
			if (zmq_setsockopt(zif_s, ZMQ_SUBSCRIBE,
					   conf.zmq_subscriptions[i],
					   strlen(conf.zmq_subscriptions[i]))
			    == -1) {
				snprintf(errstr, errstr_size,
					 "Failed to subscribe to "
					 "zmq subject \"%s\": %s",
					 conf.zmq_subscriptions[i],
					 strerror(errno));
				goto err;
			}
		}
		break;

	case ZMQ_PULL:
		/* Pull socket */
		ks_log(LOG_DEBUG, "Using ZMQ_PULL socket");

		/* No extra configuration needed */
		break;

	default:
		snprintf(errstr ,errstr_size,
			 "Unsupported zeromq.socket.type %i",
			 conf.zmq_socket_type);
		goto err;
	}


	if (conf.zmq_bind) {
		/* Bind endpoint, if configured */
		if (zmq_bind(zif_s, conf.zmq_bind) == -1) {
			snprintf(errstr, errstr_size,
				 "Failed to bind zmq socket to \"%s\": %s",
				 conf.zmq_bind, strerror(errno));
			goto err;
		}

	} else if (conf.zmq_connect) {
		/* Connect endpoint, if configured */
		if (zmq_connect(zif_s, conf.zmq_connect) == -1) {
			snprintf(errstr, errstr_size,
				 "Failed to connect zmq socket to \"%s\": %s",
				 conf.zmq_connect, strerror(errno));
			goto err;
		}
	}


	/* Create zmq recv thread */
	if ((i = pthread_create(&zif_thread, NULL, zif_main, NULL))) {
		snprintf(errstr, errstr_size,
			 "Failed to create zmq thread: %s",
			 strerror(i));
		goto err;
	}

	ks_log(LOG_DEBUG, "ZeroMQ interface started");


	return 0;

err:
	/* Common error handling: decommission what has been set up */
	if (zif_s) {
		zmq_close(zif_s);
		zif_s   = NULL;
	}
	zmq_term(zif_ctx);
	zif_ctx = NULL;

	return -1;
}


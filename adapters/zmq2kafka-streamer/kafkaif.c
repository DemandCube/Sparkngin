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
 * Kafka interface
 */


#include "zmq2kafka-streamer.h"

#include <string.h>
#include <errno.h>
#include <stdlib.h>


/**
 * Kafka delivery report callback.
 */
static void kif_dr_cb (rd_kafka_t *rk, void *payload, size_t len,
		       rd_kafka_resp_err_t err,
		       void *opaque, void *msg_opaque) {
	zmq_msg_t *msg = msg_opaque;

	if (err) {
		/* Message delivery failed: log it (if configured) */
		if (conf.flags & CONF_F_LOG_KAFKA_MSG_ERROR)
			ks_log(LOG_WARNING,
			       "Kafka message delivery failure: %s",
			       rd_kafka_err2str(err));
	}
	
	if (msg) {
		/* Let ZeroMQ free its message */
		zmq_msg_close(msg);
		free(msg);
	}
}


/**
 * Stop Kafka interface
 */
void kif_stop (void) {
	if (conf.rkt) {
		rd_kafka_topic_destroy(conf.rkt);
		conf.rkt = NULL;
	}

	if (conf.rk) {
		rd_kafka_destroy(conf.rk);
		conf.rk = NULL;
		rd_kafka_wait_destroyed(5000);
	}
}


/**
 * Start Kafka interface
 */
int kif_start (char *errstr, size_t errstr_size) {
	
	/* Configuration validation */
	if (!conf.kafka_topic) {
		snprintf(errstr, errstr_size,
			 "Mandatory configuration "
			 "property kafka.topic not configured");
		return -1;
	}

	/* Set message delivery report callback.
	 * The callback is used for delivery error reporting as well
	 * freeing the zmq message */
	rd_kafka_conf_set_dr_cb(conf.rk_conf, kif_dr_cb);

	/* Create Kafka handle */
	if (!(conf.rk = rd_kafka_new(RD_KAFKA_PRODUCER, conf.rk_conf,
				     errstr, errstr_size)))
		return -1;

	/* Create topic handle */
	if (!(conf.rkt = rd_kafka_topic_new(conf.rk, conf.kafka_topic,
					    conf.rkt_conf))) {
		snprintf(errstr, errstr_size,
			 "Failed to create Kafka topic \"%s\": %s",
			 conf.kafka_topic, strerror(errno));
		goto err;
	}

	ks_log(LOG_DEBUG, "Apache Kafka interface started");

	return 0;


err:
	/* Common error handling */
	if (conf.rkt) {
		rd_kafka_topic_destroy(conf.rkt);
		conf.rkt = NULL;
	}

	rd_kafka_destroy(conf.rk);
	conf.rk = NULL;

	return -1;
}

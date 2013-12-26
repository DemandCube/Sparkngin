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

#include "zmq2kafka-streamer.h"
#include "ezd.h"

#include <string.h>
#include <stdlib.h>
#include <getopt.h>
#include <unistd.h>
#include <signal.h>
#include <syslog.h>


/**
 * Global configuration
 */
struct conf conf;



/**
 * Configuration file reader callback.
 *
 * Set a single configuration property 'name' using value 'val'.
 * Returns 0 on success, and -1 on error in which case 'errstr' will
 * contain an error string.
 */
static int conf_set (const char *name, const char *val,
		     char *errstr, size_t errstr_size,
		     const char *path, int line, void *opaque) {
	rd_kafka_conf_res_t res;

	if (!val) {
		snprintf(errstr, errstr_size, "Expected \"key=value\" format");
		return -1;
	}

	/* Kafka configuration: let librdkafka handle it. */
	if (!strncmp(name, "kafka.", 6)) {
		const char *kname = name + 6;

		if (!strncmp(kname, "topic.", 6)) {
			/* Kafka topic configuration. */
			res = rd_kafka_topic_conf_set(conf.rkt_conf,
						      kname+6, val,
						      errstr, errstr_size);
		} else {
			/* Kafka global configuration */
			res = rd_kafka_conf_set(conf.rk_conf, kname, val,
						errstr, errstr_size);
		}

		if (res == RD_KAFKA_CONF_OK) {
			return 0;
		} else if (res != RD_KAFKA_CONF_UNKNOWN) {
			return -1;
		}

		/* FALLTHRU for unknown configuration properties */
	}

	/* Configuration options */
	if (!strcmp(name, "zeromq.socket.type")) {
		if (!strcmp(val, "rep") || !strcmp(val, "reply"))
			conf.zmq_socket_type = ZMQ_REP;
		else if (!strcmp(val, "sub") || !strcmp(val, "subscribe"))
			conf.zmq_socket_type = ZMQ_SUB;
		else if (!strcmp(val, "pull"))
			conf.zmq_socket_type = ZMQ_PULL;
		else {
			snprintf(errstr, errstr_size,
				 "Unknown zeromq.socket.type \"%s\", "
				 "try one of: reply, subscribe, pull", val);
			return -1;
		}

	} else if (!strcmp(name, "zeromq.identity")) {
		if (conf.zmq_id)
			free(conf.zmq_id);
		conf.zmq_id = strdup(val);

	} else if (!strcmp(name, "zeromq.bind")) {
		if (conf.zmq_connect) {
			snprintf(errstr, errstr_size,
				 "zeromq.bind and zeromq.connect are "
				 "mutually exclusive");
			return -1;
		}
		if (conf.zmq_bind)
			free(conf.zmq_bind);
		conf.zmq_bind = strdup(val);

	} else if (!strcmp(name, "zeromq.connect")) {
		if (conf.zmq_bind) {
			snprintf(errstr, errstr_size,
				 "zeromq.bind and zeromq.connect are "
				 "mutually exclusive");
			return -1;
		}
		if (conf.zmq_connect)
			free(conf.zmq_connect);
		conf.zmq_connect = strdup(val);

	} else if (!strcmp(name, "zeromq.subscribe")) {
		if (conf.zmq_subscriptions)
			free(conf.zmq_subscriptions);
		conf.zmq_subscriptions_cnt =
			ezd_csv2array(&conf.zmq_subscriptions, val);

	} else if (!strcmp(name, "kafka.topic"))
		conf.kafka_topic = strdup(val);

	else if (!strcmp(name, "kafka.partition"))
		conf.kafka_partition = atoi(val);

	else if (!strcmp(name, "log.kafka.msg.error")) {
                if (ezd_str_tof(val))
                        conf.flags |= CONF_F_LOG_KAFKA_MSG_ERROR;
                else
                        conf.flags &= ~CONF_F_LOG_KAFKA_MSG_ERROR;

	} else if (!strcmp(name, "log.level"))
		conf.log_level = atoi(val);

	else if (!strcmp(name, "daemonize"))
		conf.daemonize = ezd_str_tof(val);

	else {
		snprintf(errstr, errstr_size,
			 "Invalid configuration property \"%s\"", name);
		return -1;
	}


	return 0;
}


/**
 * Print command line usage
 */
static void usage (const char *me) {
	printf("zmq2kafka-streamer version %s\n"
	       "ZeroMQ -> Kafka bridge\n"
	       "\n"
	       "Usage: %s [options]\n"
	       "\n"
	       "Options:\n"
	       "  -c <path>   Config file path (default: %s)\n"
	       "  -p <path>   Pidfile path (default: %s)\n"
	       "  -d          Enable debugging\n"
	       "  -D          Do not daemonize/background\n"
	       "\n",
	       ZKS_VERSION_STR, me,
	       ZKS_CONF_PATH_DEFAULT,
	       ZKS_PIDFILE_PATH_DEFAULT);
	exit(1);
}


/**
 * Flag program for termination.
 */
static void term (int sig) {
	/* Die instantly on second termination signal */
	if (!conf.run)
		exit(1);

	conf.run = 0;
}



int main (int argc, char **argv) {
	char *conf_path    = ZKS_CONF_PATH_DEFAULT;
	char *pidfile_path = ZKS_PIDFILE_PATH_DEFAULT;
	char errstr[512];
	char c;

	/* Default configuration */
	conf.run             = 1;
	conf.log_level       = 6;
	conf.daemonize       = 1;

	conf.zmq_socket_type = ZMQ_PULL;

	conf.kafka_partition = RD_KAFKA_PARTITION_UA; /* Random partitioning */
	conf.rk_conf         = rd_kafka_conf_new();
	conf.rkt_conf        = rd_kafka_topic_conf_new();

	conf.flags          |= CONF_F_LOG_KAFKA_MSG_ERROR;


	/* Parse command line arguments */
	while ((c = getopt(argc, argv, "c:p:dDh")) != -1) {
		switch (c)
		{
		case 'c':
			conf_path = optarg;
			break;
		case 'p':
			pidfile_path = optarg;
			break;
		case 'd':
			conf.log_level = 7;
			break;
		case 'D':
			conf.daemonize = 0;
			break;
		case 'h':
		default:
			usage(argv[0]);
			break;
		}
	}

	/* Read config file */
	if (ezd_conf_file_read(conf_path, conf_set,
			       errstr, sizeof(errstr), NULL) == -1) {
		fprintf(stderr, "%s\n", errstr);
		exit(1);
	}

	/* Go to background if configured to do so */
	if (conf.daemonize) {
		if (ezd_daemon(10, errstr, sizeof(errstr)) == -1) {
			fprintf(stderr, "%s\n", errstr);
			exit(1);
		}
		/* We're now in the child process */
	}

	/* Check and create pidfile */
	if (ezd_pidfile_open(pidfile_path, errstr, sizeof(errstr)) == -1) {
		fprintf(stderr, "%s\n", errstr);
		exit(1);
	}

	/* Set up logging output to syslog */
	openlog("zmq2kafka-streamer",
		LOG_PID | (!conf.daemonize ? LOG_PERROR : 0), LOG_DAEMON);

	/* Start ZeroMQ interface */
	if (zif_start(errstr, sizeof(errstr)) == -1) {
		fprintf(stderr, "%s\n", errstr);
		exit(1);
	}

	/* Start Kafka interface */
	if (kif_start(errstr, sizeof(errstr)) == -1) {
		fprintf(stderr, "%s\n", errstr);
		exit(1);
	}

	/* Finalize daemonization */
	if (conf.daemonize)
		ezd_daemon_started();

	/* Termination signal setup */
	signal(SIGINT, term);
	signal(SIGTERM, term);


	/* Main loop */
	while (conf.run) {
		/* Poll Kafka for delivery report callbacks. */
		rd_kafka_poll(conf.rk, 1000);
	}

	/* Termination */
	zif_stop();
	kif_stop();
	ezd_pidfile_close();

	exit(0);
}

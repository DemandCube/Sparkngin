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

#pragma once

#include <syslog.h>
#include <librdkafka/rdkafka.h>
#include <zmq.h>

#define ZKS_CONF_PATH_DEFAULT     "zmq2kafka-streamer.conf"
#define ZKS_PIDFILE_PATH_DEFAULT  "zmq2kafka-streamer.pid"

/* Global configuration container */
struct conf {
	int               run;

	/* ZeroMQ */
	int               zmq_socket_type;
	char             *zmq_id;
	char            **zmq_subscriptions;
	int               zmq_subscriptions_cnt;
	char             *zmq_bind;
	char             *zmq_connect;

	/* Kafka */
	char             *kafka_topic;
	int               kafka_partition;
	rd_kafka_t       *rk;
	rd_kafka_topic_t *rkt;
	rd_kafka_conf_t       *rk_conf;
	rd_kafka_topic_conf_t *rkt_conf;

	/* Generic */
	int               log_level;
	int               daemonize;
	                   
	int               flags;
#define CONF_F_LOG_KAFKA_MSG_ERROR  0x1
};

extern struct conf conf;


/**
 * Log macro
 */
#define ks_log(LEVEL,FMT...) do {		\
	if (LEVEL <= conf.log_level)		\
		syslog(LEVEL, FMT);		\
	} while (0)





/**
 * ZeroMQ interface
 */
void zif_stop (void);
int zif_start (char *errstr, size_t errstr_size);


/**
 * Kafka interface
 */
void kif_stop (void);
int kif_start (char *errstr, size_t errstr_size);

zmq2kafka-streamer - ZeroMQ -> Kafka producer bridge
====================================================

zmq2kafka-streamer is a utility to read messages from ZeroMQ and
produce them to an Apache Kafka cluster.

Supported ZeroMQ socket types are SUB and PULL.




# Instructions

## Dependencies
     libzmq-dev
     librdkafka (https://github.com/edenhill/librdkafka or Debian: librdkafka-dev)

## Build
    make


## Install
    # To /usr/local
    make install

    or:

    DESTDIR=/other/place make install


## Configuration

zmq2kafka-streamer is configured through a `key=value` formatted
configuration file.
The supported configuration properties are documented
in `zmq2kafka-streamer.conf.sample`.



## Running zmq2kafka-streamer

    # Run in background (default)
    zmq2kafka-streamer -c <conf-file-path>

    # Run in foreground (-D) with debugging (-d)
    zmq2kafka-streamer -c <conf-file-path> -D -d

    # Alternate pid-file path
    zmq2kafka-streamer -p <pid-file-path> ..
 

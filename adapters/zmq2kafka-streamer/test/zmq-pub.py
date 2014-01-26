#!/usr/bin/python

import zmq
import time

if __name__ == "__main__":
    context = zmq.Context()

    pub = context.socket(zmq.PUB)
    pub.bind("tcp://*:5555")

    ztopic = "test1"

    for n in xrange(10):
        msg = "Pubmsg %d" % n
        pub.send("%s %s" % (ztopic, msg))
        time.sleep(0.5)

    time.sleep(1)

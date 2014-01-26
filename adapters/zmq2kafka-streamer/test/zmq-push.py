#!/usr/bin/python

import zmq
import time

if __name__ == "__main__":
    context = zmq.Context()

    push = context.socket(zmq.PUSH)
    push.bind("tcp://0:5555")

    for n in xrange(100):
        msg = "Pushmsg %d" % n
        push.send(msg)
        #time.sleep(0.5)

    time.sleep(1) # Time to flush


import zmq


def test():
    context = zmq.Context()

    subscriber = context.socket(zmq.SUB)
    subscriber.connect('tcp://localhost:8001')

    subscriber.setsockopt(zmq.SUBSCRIBE, b'')
    while True:
        msg = subscriber.recv()
        print( msg ),


if __name__ == '__main__':
    test()



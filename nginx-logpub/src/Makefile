
OBJS=logpub.o
LFLAGS=-lzmq
CFLAGS=-I../inc

@all: $(OBJS)
	rm -f logpub
	$(CXX) -o logpub $(LFLAGS) \
		$(OBJS)

logpub.o: logpub.cpp 
	$(CXX) -c $(INC) $(CFLAGS) logpub.cpp

clean:
	rm -f logpub
	rm -f *.o



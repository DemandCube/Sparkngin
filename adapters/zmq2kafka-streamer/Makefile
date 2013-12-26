
NAME=zmq2kafka-streamer

DESTDIR?=/usr/local
VER     := `git describe --abbrev=6 --tags HEAD --always`
CFLAGS+=-DZKS_VERSION_STR="\"$(VER)\""

SRCS=	main.c zeromqif.c kafkaif.c ezd.c

OBJS=	$(SRCS:.c=.o)
DEPS=	${OBJS:%.o=%.d}

LIBS=	-lzmq -lrdkafka -lrt -lpthread -lz

CFLAGS+=-O2 -Wall -Werror -Wfloat-equal -Wpointer-arith -g

# Clang warnings to ignore
ifeq ($(CC),clang)
	CFLAGS+=-Wno-gnu-designator
endif

# Profiling
#CFLAGS+=-O0
#CFLAGS += -pg
#LDFLAGS += -pg

.PHONY:

all: $(NAME)

%.o: %.c
	$(CC) -MD -MP $(CFLAGS) -c $<

$(NAME): $(OBJS)
	$(CC) $(LDFLAGS) $(OBJS) -o $(NAME) $(LIBS)

install:
	if [ "$(DESTDIR)" != "/usr/local" ]; then \
		DESTDIR="$(DESTDIR)/usr"; \
	else \
		DESTDIR="$(DESTDIR)" ; \
	fi ; \
	install -t $$DESTDIR/bin $(NAME)

test: .PHONY
	make -C test

clean:
	rm -f $(OBJS) $(DEPS) $(NAME)

-include $(DEPS)

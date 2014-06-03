#!/bin/bash

cygwin=false
case "`uname`" in
  CYGWIN*) cygwin=true;;
esac

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`
APP_DIR=`cd $bin/..; pwd; cd $bin`
JAVACMD=$JAVA_HOME/bin/java

if $cygwin; then
  APP_DIR=`cygpath --absolute --windows "$APP_DIR"`
fi

PID_FILE="$APP_DIR/bin/kafka.pid"
APP_OPT="-Dapp.dir=$APP_DIR -Duser.dir=$APP_DIR"
LOG_OPT="-Dlog4j.configuration=file:$APP_DIR/config/log4j.properties"

MAIN_CLASS="com.neverwinterdp.server.Server"

function startServer {
  SERVER_NAME=$1
  APP_OPT="$APP_OPT -Dserver.name=$SERVER_NAME"
  nohup $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $LOG_OPT $MAIN_CLASS "$@" <&- &>/dev/null &
  #printf '%d' $! > $SERVER_NAME.pid
}

function runServer {
  SERVER_NAME=$1
  APP_OPT="$APP_OPT -Dserver.name=$SERVER_NAME"
  $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $LOG_OPT $MAIN_CLASS "$@"
}


startServer -Pserver.name=zookeeper -Pserver.roles=zookeeper
startServer -Pserver.name=kafka -Pserver.roles=kafka
startServer -Pserver.name=sparkngin -Pserver.roles=sparkngin

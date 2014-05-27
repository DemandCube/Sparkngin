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

PID_FILE="$APP_DIR/bin/sparkngin.pid"
APP_OPT="-Dapp.dir=$APP_DIR"
LOG_OPT="-Dlog4j.configuration=file:$APP_DIR/config/log4j.properties"

SPARKNGIN_MAIN_CLASS="com.neverwinterdp.sparkngin.SparknginServer"

function runConsole {
  $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $LOG_OPT $SPARKNGIN_MAIN_CLASS "$@"
}

function runDeamon {
  nohup $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $LOG_OPT $SPARKNGIN_MAIN_CLASS "$@" <&- &>/dev/null &
  printf '%d' $! > $PID_FILE
}

function killDeamon {
   kill -9 `cat $PID_FILE` && rm -rf $PID_FILE
}

function hello {
  HELLO_MAIN_CLASS="com.neverwinterdp.sparkngin.HelloSparkngin"
  $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $LOG_OPT $HELLO_MAIN_CLASS "$@"
}

COMMAND=$1
shift

if [ "$COMMAND" = "run" ] ; then
  runConsole "$@"
elif [ "$COMMAND" = "deamon" ] ; then
  runDeamon "$@"
elif [ "$COMMAND" = "kill" ] ; then
  killDeamon "$@"
elif [ "$COMMAND" = "hello" ] ; then
  hello "$@"
elif [ "$COMMAND" = "quick-test" ] ; then
  echo "******************************************************************************"
  echo "This quick test will:"
  echo "  1. Launch the sparkngin server in the deamon mode"
  echo "  2. Run the hello sparkngin to send the messages to the sparkngin server"
  echo "  4. Kill the sparkngin deamon once the hello job terminate"
  echo "******************************************************************************"
  runDeamon "$@"
  echo "Wait 10s to make sure the sparkngin is launched"
  sleep 10
  hello "$@"
  killDeamon "$@"
else
  echo "Avaliable Commands: "
  echo "  run:         Run sparkngin in the console mode"
  echo "  deamon:      Run sparkngin in the deamon mode"
  echo "  kill:        Kill sparkngin deamon"
  echo "  hello:       Send messages to sparkngin"
  echo "  quick-test:  Launch sparkngin in deamon mode, send messages to sparkngin, kill sparkngin"
fi

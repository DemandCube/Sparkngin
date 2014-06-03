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

MAIN_CLASS="com.neverwinterdp.server.shell.Shell"

function runShell {
  $JAVACMD -Djava.ext.dirs=$APP_DIR/libs $APP_OPT $MAIN_CLASS "$@"
}


runShell $@

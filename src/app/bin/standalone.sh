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
LOG_OPT="-Dlog4j.configuration=file:$APP_DIR/config/standalone-log4j.properties"

MAIN_CLASS="com.neverwinterdp.sparkngin.Main"

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

COMMAND=$1
shift
echo ""
echo "************************************************************************************************************"
echo "Standalone Sparkngin"
echo "************************************************************************************************************"
echo ""

if [ "$COMMAND" = "run" ] ; then
  runServer \
    --data-dir $APP_DIR/data --forwarder nulldev \
    --sparkngin:tracking.site.log-topic=site.log \
    --sparkngin:tracking.site.extract-headers=Host.*,content.* \
    --kafka:metadata.broker.list=127.0.0.1:9092

elif [ "$COMMAND" = "deamon" ] ; then
    cluster_exec "ps -x | grep NeverwinterDP"
else
  echo "cluster command options: "
  echo "  run     : To launch the standalone sparkngin in console mode "
  echo "  deamon  : To launch the standalone sparkngin in deamon mode "
fi

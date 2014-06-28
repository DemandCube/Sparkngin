
:echo "List the current server status"
:echo "==============================="
server ping

:echo "list the server and services registration"
:echo "=======================================--"
server registration

:echo "Install zookeeper on the zookeeper server role"
:echo "==============================================="
module install  \
  -Pmodule.data.drop=true \
  -Pzk:clientPort=2181 \
  --member-role zookeeper --autostart --module Zookeeper

:echo "Install kafka on the kafka server role"
:echo "========================================="
module install \
  -Pmodule.data.drop=true \
  -Pkafka:port=9092 -Pkafka:zookeeper.connect=127.0.0.1:2181 \
  --member-role kafka --autostart --module Kafka

:echo "Install sparkngin on the sparkngin server role"
:echo "=============================================="
module install \
  -Pmodule.data.drop=true \
  -Pforwarder-class=com.neverwinterdp.sparkngin.http.KafkaMessageForwarder \
  --member-role sparkngin --autostart --module Sparkngin

:echo "list the server and services registration"
:echo "=======================================--"
server registration

:sleep 1000

sparkngin hello --num-message 100000 --topic metrics.consumer
:sleep 30000
server metric
server metric-clear --name-exp *

sparkngin hello --num-message 1000000 --topic metrics.consumer
server metric

:echo "Uninstall Sparkngin module on the sparkngin role servers"
:echo "================================================"
module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin

:echo "Uninstall Kafka module on the kafka role servers"
:echo "================================================"
module uninstall --member-role kafka --timeout 20000 --module Kafka

:echo "Uninstall Zookeeper module on the zookeeper role servers"
:echo "========================================================"
module uninstall --member-role zookeeper --timeout 20000 --module Zookeeper

:echo "list the server and services registration after uninstall kafka and zookeeper service"
:echo "====================================================================================="
server registration


:echo "shutdown the cluster"
:echo "===================="
server exit

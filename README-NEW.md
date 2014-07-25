#Sparkngin#

Sparkngin is a high-performance persistent message stream engine built on netty and java chronicle. Sparkngin can function as logging, event or message streaming solution.

Sparkngin contains:

1. A persistent event/message logging buffer implement on top of java chronicle
2. An event/message route handler that persist the event/message and forward the messages to the queuengin or a data repository. The route handler is a plugin for the http service of the NeverwinterDP-Commons/netty project. 
4. SparknginClusterService is a netty http service + the sparkgnin route handler to persist and forward the message. The SparknginClusterService is manageable by the cluster mangement tool.

##SparknginClusterService##

To install, you can call this command from the cluster shell or the cluster gateway

```
module install 
  -Pforwarder-class=com.neverwinterdp.sparkngin.http.NullDevMessageForwarder
  -Phttp-listen-port=8080
  -Phttp-www-dir=path/webapp
  --member-role sparkngin --autostart --module Sparkngin
```
Where the parameter:
  

*  -P is the property name and value
*  -Pforwarder-class to set the forwarder plugin. The forwarder can be as simple as NullDevMessageForwarder to throw away the messages. It is convienent for testing case. Or the KafkaMessageForwarder to forward the message to the kafka server. 
*  -Phttp-listen-port to set http service listen port
*  -Phttp-www-dir to set the www static files directory
*  --member-role or --member-name or --member-uuid to select the target servers
*  --autostart to launch the service automatically affer the installation
*  --module Sparkngin to select the sparkngin module to install

To uninstall

```
module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin
```

#Build And Develop#

##Build With Gradle##

1. cd Sparkngin
2. gradle clean build install

##Eclipse##

To generate the eclipse configuration

1. cd path/Sparkngin
2. gradle eclipse

To import the project into the  eclipse

1. Choose File > Import
2. Choose General > Existing Projects into Workspace
3. Check Select root directory and browse to path/Sparkngin
4. Select all the projects then click Finish
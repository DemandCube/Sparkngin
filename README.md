Sparkngin
==========
Pronounced Spark Engine  

Sparkngin is a high-performance persistent message logger built on Nginx

Out of the box includes:
- persistent kafka client Realtime streaming logs to Kafka
- heart beat
- log cleanup
- Connection retries when it looses connection to log destination
- Log persistence if the log consumer connection is down

Additonal:
- Monitoring with Ganglia
- Heart Alerting with Nagios


This is part of [NeverwinterDP](https://github.com/DemandCube/NeverwinterDP)

Community
====
- [Mailing List](https://groups.google.com/forum/#!forum/sparkngin)
- IRC channel #Sparkngin on irc.freenode.net


The Problem
======
The core problem is how to log data from a rest call and log it in high-performance way that 
allows for the delivery of messages to Kafka even when the connection is down.

Potential Implementation Strategies
======
There is a question of how to implement quaranteed delivery of logs to kafka.  
- Implement Avro Protocol?
- nginx write to ipc pipe -> secondary application that logs to disk and kafka
- nginx write to zmq -> secondary application that logs to disk and kafka
- nginx direct kafka driver that also spools to disk


Milestones
======
- [ ] Sparkngin -> Zeromq (raw)
- [ ] Sparkngin -> Zeromq (NW protocol V1)
- [ ] Zeromq -> Kafka
- [ ] Zeromq -> Flume
- [ ] Zeromq -> Syslog
- [ ] Ganglia Integration
- [ ] Nagios Integration
- [ ] Sparkngin Client (raw)
- [ ] Sparkngin Client (NW protocol V1)
- [ ] Heartbeat Agent
- [ ] Unix Man page
- [ ] Guide
- [ ] Build System - cmake or autotools
- [ ] Untar and Deploy - Work out of the box
- [ ] CentOS Package
- [ ] CentOS Repo Setup and Deploy of CentOS Package
- [ ] RHEL Package
- [ ] RHEL Repo Setup and Deploy of CentOS Package
- [ ] Mac DMG
- [ ] ZeroConf system
- [ ] HA logger
- [ ] Sparkngin/Ambari Deployment
- [ ] Sparkngin/Ambari Monitoring/Ganglia
- [ ] Sparkngin/Ambari Notification/Nagios
- [ ] 

Model Project to Eval
====
- <https://github.com/FRiCKLE/ngx_zeromq>
- <https://github.com/tailhook/zerogw>
- <http://openresty.org/>

Community Threads
====
- <http://stackoverflow.com/questions/8765385/mongrel2-vs-nginxzeromq>
- <http://stackoverflow.com/questions/15287196/websockets-behind-nginx-triggered-by-zeromq>

Resources to Learn Development
====
- <http://www.evanmiller.org/nginx-modules-guide.html>
- <http://www.evanmiller.org/nginx-modules-guide-advanced.html>
- <http://agentzh.org/misc/slides/nginx-conf-scripting/#1>
- <http://agentzh.org/misc/slides/recent-dev-nginx-conf/#1>
- <http://antoine.bonavita.free.fr/nginx_mod_dev_en.html>
- Hello World <http://blog.zhuzhaoyuan.com/2009/08/creating-a-hello-world-nginx-module/>
- Hello World Extended <http://usamadar.com/2012/09/02/writing-a-custom-nginx-module/>
- Other Hello World <http://nutrun.com/weblog/2009/08/15/hello-world-nginx-module.html>
- <https://github.com/perusio/nginx-hello-world-module>
- Intro Slides on Dev <http://www.slideshare.net/trygvevea/extending-functionality-in-nginx-with-modules>

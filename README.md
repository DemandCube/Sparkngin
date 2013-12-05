Sparkngin
==========
Pronounced Spark Engine  

Sparkngin is a high-performance persistent message stream engine built on Nginx.  Sparkngin can function as logging, event or message streaming solution.  When used with Neverwinter Data Platform Sparkngin can stream data to data repositories like Hive, Hbase, Storm and HDFS.

- This is part of [NeverwinterDP](https://github.com/DemandCube/NeverwinterDP)

The Problem
======
The core problem is how to stream data from a rest calls (an endpoint) and send it through a horizonatally scalable HA high-performance way that 
allows for the delivery of messages to an end system like (Kafka, Storm, HDFS ...)

Features
======

Out of the box includes:
- Data, Log, Event, Message - Ingestion/Streaming
- Heart Beat
- Log cleanup
- Persistent kafka client Realtime streaming logs to Kafka
- Connection retries when it looses connection to log destination
- Log persistence if the log producer connection is down
- Monitoring with Ganglia
- Heart Alerting with Nagios

Table of Contents
- TODO


Community
======
- [Mailing List](https://groups.google.com/forum/#!forum/sparkngin)
- IRC channel #Sparkngin on irc.freenode.net

How to Contribute
======

There are many ways you can contribute towards the project. A few of these are:

**Jump in on discussions**: It is possible that someone initiates a thread on the [Mailing List](https://groups.google.com/forum/#!forum/sparkngin) describing a problem that you have dealt with in the past. You can help the project by chiming in on that thread and guiding that user to overcome or workaround that problem or limitation.

**File Bugs**: If you notice a problem and are sure it is a bug, then go ahead and file a [GitHub Issue](https://github.com/DemandCube/Sparkngin/issues?state=open). If however, you are not very sure that it is a bug, you should first confirm it by discussing it on the [Mailing List](https://groups.google.com/forum/#!forum/sparkngin).

**Review Code**: If you see that a [GitHub Issue](https://github.com/DemandCube/Sparkngin/issues?state=open) has a "patch available" status, go ahead and review it. The other way is to review code submited with a [pull request](https://github.com/DemandCube/Sparkngin/pulls), it is the prefered way.  It cannot be stressed enough that you must be kind in your review and explain the rationale for your feedback and suggestions. Also note that not all review feedback is accepted - often times it is a compromise between the contributor and reviewer. If you are happy with the change and do not spot any major issues, then +1 it.

**Provide Patches**: We encourage you to assign the relevant [GitHub Issue](https://github.com/DemandCube/Sparkngin/issues?state=open) to yourself and supply a patch or [pull request](https://github.com/DemandCube/Sparkngin/pulls) for it. The patch you provide can be code, documentation, tests, configs, build changes, or any combination of these.

How to Submit - Patches/Code
======

1. **Create a patch**
  * Make sure it applies cleanly against trunk
1. **Test**
  * If code supply tests and unit test
1. **Propose New Features or API**
  * Document the new Feature or API in the Wiki, the get consensus by discussing on the mailing list
1. **Open a GitHub Ticket**
  * Create the patch or pull request, attach your patch or pull request to the Issue.
    * Your changes should be well-formated, readable and lots of comments
    * Add tests
    * Add to documentation, especially if it changes configs
    * Add documentation so developers, can understand the feature or API to continue to contribute
  * Document information about the issue and approach you took to fix it and put it in the issue.
  * Send a message on the mailing list requesting a commiter review it.
  * Nag the list if we (commiters) don't review it and followup with us.

1. **How to create a patch file**: 
  * The preferred naming convention for Sparkngin patches is `SPARKNGIN-12345-0.patch` where `12345` is the Issue number and `0` is the version of the patch. 
  * Patch Command:
    * `$ git diff > /path/to/SPARKNGIN-1234-0.patch`

1. **How to apply someone else's patch file**: 
```
$ cd ~/src/Sparkngin # or wherever you keep the root of your Sparkngin source tree 
$ patch -p1 < SPARKNGIN-1234-0.patch # Default when using git diff
$ patch -p0 < SPARKNGIN-1234-0.patch # When using git diff --no-prefix
```

1. Reviewing Patches
  * [Find issues with label "Patch Available"](https://github.com/DemandCube/Sparkngin/issues?labels=patch+avaliable&page=1&state=open), look over and give your feedback in the issue as necessary.  If there are questions discuss in the [Mailing List](https://groups.google.com/forum/#!forum/sparkngin).


## Git Workflow
  * [Suggested Git Workflows](https://cwiki.apache.org/confluence/display/KAFKA/Git+Workflow)

## Github Help
  * [How push from your local repo to github](https://help.github.com/articles/pushing-to-a-remote#pushing-a-branch)
  * [How to send a pull request](https://help.github.com/articles/using-pull-requests)
  * [How to sync a forked repo on github](https://help.github.com/articles/syncing-a-fork)

## TODO Document the recommended workflow in Github 
  * fork repo -> make changes -> sync forked repo local -> push to github forked repo -> do pull request




Potential Implementation Strategies
======
There is a question of how to implement quaranteed delivery of logs to kafka.  
- Implement Avro Protocol?
- nginx write to ipc pipe -> secondary application that logs to disk and kafka
- nginx write to zmq -> secondary application that logs to disk and kafka
- nginx direct kafka driver that also spools to disk


Example Flow
=====
Application sending messages -> Sparkngin [ Nginx -> Zeromq (Publisher) -> Zeromq (Subscriber) -> Kafka (Client call "Producer") ] -> Kafka -> (Client called consumer) -> Some Application

Roadmap
======
- M0.1
- [Issue: 1 - Document proposed high level Architecture for Sparkngin](https://github.com/DemandCube/Sparkngin/issues/1)
- [Issue: 2 - Create mockup of new configuration directives that Sparkngin will provide](https://github.com/DemandCube/Sparkngin/issues/2)
- [Issue: 3 - Create nginx configuration for Sparkngin](https://github.com/DemandCube/Sparkngin/issues/3)
- [Issue: 4 - Create sample zeromq socket reader python commandline application](https://github.com/DemandCube/Sparkngin/issues/4)
- [Issue: 5 - Create sample zeromq socket reader c commandline application](https://github.com/DemandCube/Sparkngin/issues/5)

Feature Todo
======
- [ ] Architecture Proposal
- [ ] Sparkngin -> Zeromq (raw)
- [ ] Sparkngin -> Zeromq (NW protocol V1 - see below) 
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
- [ ] Log stash integration
- [ ] Elastic search integration
- [ ] Sparkngin/Ambari Deployment
- [ ] Sparkngin/Ambari Monitoring/Ganglia
- [ ] Sparkngin/Ambari Notification/Nagios
- [ ] Event Schema Registration - json, avro, thrift protobuffs


NW Protocol V1
=====
Purpose is to provide standard event data that is used to allow for systematic monitoring, analytics, retries and timebased partition notifications (Aka send a message when all data from hour 1 is sent)
- timestamp
- ip of referrer
- topic
- env
- version
- submitted timestamp

Model Project to Eval
====
- <https://github.com/FRiCKLE/ngx_zeromq>
- <https://github.com/tailhook/zerogw>
- <http://openresty.org/>

Community Threads
====
- <http://stackoverflow.com/questions/8765385/mongrel2-vs-nginxzeromq>
- <http://stackoverflow.com/questions/15287196/websockets-behind-nginx-triggered-by-zeromq>

Resources to Learn Development Nginx
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
- In russian <http://www.grid.net.ru/nginx/nginx-modules.html>

Example Nginx Modules
- <http://wiki.nginx.org/3rdPartyModules>
- Redis Log Module <http://www.binpress.com/app/nginx-redislog-module/998>
- Socket Log Module <http://www.binpress.com/app/nginx-socketlog-module/1030>

Contributors
=====
- [Steve Morin](https://github.com/smorin)
- [Juan Manuel Clavijo](https://github.com/PROM3TH3U5)
- [Cui Yingjie](https://github.com/nihuo)
- [Ben Speakmon](https://github.com/bspeakmon)
 
FAQ
=====

- Why Sparkngin?

Sparkngin is mean to solve the short coming of realtime event streaming using restful endpoint.  Utilizing the logging and other connections in nginx is hard to configure and has limitations.

- Why trust Sparkngin?

Sparkngin is built on top of two main projects [Nginx](http://wiki.nginx.org/Main) which is the [worlds second most popular web server](http://news.netcraft.com/archives/2012/01/03/january-2012-web-server-survey.html) and [Zeromq](http://zeromq.org/) a high performance networking library.  Both provide a very solid core to realtime event streaming.  If you have questions about [why nginx](http://wiki.nginx.org/WhyUseIt), click the link.  Some people who use it are Facebook, [PInterest, Airbnb, Netflix, Hulu and Wordpress among others](http://wiki.nginx.org/Main).  Here is a summary of some nginx [benefits and features](http://www.wikivs.com/wiki/Apache_vs_nginx).

- What is the difference between timestamp and submitted timestamp

The concept is the timestamp is the system timestamp of that machine, which the submitted timestamp is a optional timestamp you might submit in the request.  So if for example you have data that you want to submit an hour later, you might want to organize it around a submitted timestamp rather than by the system recorded timestamp of the user.



Design
=====

####Architecture
Whole system is consit of two parts: 

1. Nginx module: Core
2. Adaptors: They can be used to link Sparkngin Nginx module with 3rdparty system, e.g. Kafka, Flume....

####Sparkngin Nginx module
- Init zeromq publisher mode in nginx init master callback
- Create a shared memory buffer in init master callback, the buffer will be used to cache log data
- Publish Nginx http log activity data via zeromq
- If there are not any subscriber, log data will be stored into buffer. The buffer is orgnized as a loop linked list. Oldest log will be overwritten when the buffer is full. 


####Configuration of Sparkngin Nginx module
- listen port
- cache buffer size
- gzip mode on/off
- output format: json / plain text / binary
- output fields customization


####HTTP API
- /log: log event
The interface could be used to added log data into log stream. 

e.g. /log?level=info&type=http&stimestamp=134545634345&ver=1.0&topic=test&env=testdata&ip=1.1.1.1

- /status: some statistic results of running which is formated in json
- /imok: get heart beat signal


####Log Field
- ip
- timestamp
- submitted timestamp
- type
- level
- topic
- env
- version
- referrer
- user agent
- data which is parsed from cookie




Configuration directives
=====
sparkngin_listen
---------------
* **syntax**: `sparkngin_listen port`
* **default**: `7000`
* **context**: `http`

Set listen port of zeromq publisher. 


sparkngin_buf_size
---------------
* **syntax**: `sparkngin_buf_size size`
* **default**: `4M`
* **context**: `http`

Set log data cache buffer size.


sparkngin_gzip
---------------
* **syntax**: `sparkngin_gzip on/off`
* **default**: `off`
* **context**: `http`

Set gzip switch on/off. 


sparkngin_format
---------------
* **syntax**: `sparkngin_format (json|plain) ['delimiter']`
* **default**: `plain ' '`
* **context**: `http`

Set output format:

- json		- log data will be exported in json format
- plain		- log data will be exported in plain text format, each field is sperated by delimiter.


sparkngin_root_loc
---------------
* **syntax**: `sparkngin_root_loc`
* **default**: ``
* **context**: `location`

Set sparkngin root location.

e.g.

With below configuration, we can access /sn/imok, /sn/stat, /sn/log.

>  location /sn {
>            sparkngin_root_loc ;
>        }

       
  


sparkngin_fields
---------------
* **syntax**: `sparkngin_fields fields list`
* **default**: `%version% %ip% %time_stamp% %level% %topic% %user-agent% %referrer% %cookie%`
* **context**: `http`

Set output fields. 
Below are available fields:

- version
- ip
- time_stamp
- submitted_timestamp
- level			- info, warnning, error...
- topic
- user-agent
- referrer
- cookie		- whole cookie data
- cookie_[cooklie_key]		- e.g. %cookie_user_id%, will parse 'user_id' value from cookie. 
- env


Log Config Spec
==============
- [Varnish Kafka Config Example](https://github.com/wikimedia/varnishkafka/blob/master/varnishkafka.conf.example)

Sample Nginx configuration
=====

```
worker_processes  2;

events {
    worker_connections  1024;
}

http {
    sparkngin_listen 7000;
    sparkngin_gzip on;
    sparkngin_format json;
    sparkngin_fields %version% %ip% %time_stamp% %level% %topic% %user-agent% %referrer% %cookie%;
    
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;

    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   html;
            index  index.html index.htm;
        }
        
        location /sparkngin {
            sparkngin_root_loc ;
        }
    }
}
```

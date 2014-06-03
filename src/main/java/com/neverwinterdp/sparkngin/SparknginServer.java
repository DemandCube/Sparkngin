package com.neverwinterdp.sparkngin;

import java.util.Properties;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.module.SparknginModule;
import com.neverwinterdp.sparkngin.http.NullDevMessageForwarder;
import com.neverwinterdp.sparkngin.http.KafkaMessageForwarder;
import com.neverwinterdp.util.IOUtil;

public class SparknginServer {
  static public class Options {
    @Parameter(
      names = "-config", 
      description = "The configuration file in the properties format"
    )
    String configFile;
    
    @Parameter(
       names = "-kafka-connect", 
       description = "The list of the kafka server in fomat host:port, separate by comma"
    )
    String kafkaConnect = "127.0.0.1:9092";
  
    @Parameter(
        names = "-forwarder", 
        description = "Forward the message to a null device or kafka queue engine"
     )
     String forwarder = "nulldev";
    
    @Parameter(
        names = "-queue-buffer", 
        description = "size of the queue"
    )
    int queueBuffer = 1000;
  }
  
  static public void main(String[] args) throws Exception {
    Options options = new Options();
    new JCommander(options, args).usage() ;
    
    Properties properties = new Properties() ;
    if(options.configFile != null) {
      properties.load(IOUtil.loadRes(options.configFile));
    } else {
      properties.put("server.group", "NeverwinterDP") ;
      properties.put("server.cluster-framework", "hazelcast") ;
      properties.put("server.roles", "master") ;
      properties.put("server.available-modules", SparknginModule.class.getName()) ;
      properties.put("server.install-modules", SparknginModule.class.getName()) ;
      properties.put("server.install-modules-autostart", "true") ;
      
      properties.put("sparkngin.sparkngin.http-listen-port", "8080") ;
      properties.put("sparkngin.sparkngin.queue-buffer", "1000") ;
      properties.put("sparkngin.forwarder.class", NullDevMessageForwarder.class.getName()) ;
      properties.put("sparkngin.forwarder.kafka-broker-list", "127.0.0.1:9092") ;
    }
    
    if("nulldev".equals(options.forwarder)) {
      properties.put("sparkngin.forwarder.class", NullDevMessageForwarder.class.getName()) ;
    } else {
      properties.put("sparkngin.forwarder.class", KafkaMessageForwarder.class.getName()) ;
    }
    properties.put("sparkngin.sparkngin.queue-buffer", Integer.toString(options.queueBuffer)) ;
    //zkServerProps.put("zookeeper.config-path", "") ;
    if(options.kafkaConnect != null) {
      properties.put("sparkngin.forwarder.kafka-broker-list", options.kafkaConnect) ;
    }
    
    //Server server = Server.create(properties);
    Thread.currentThread().join();
  }
}
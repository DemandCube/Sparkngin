package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.util.FileUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginClusterBuilder {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("app.config.dir", "src/app/config") ;
    System.setProperty("log4j.configuration", "file:src/app/config/log4j.properties") ;
  }
  
  public static String TOPIC = "metrics.consumer" ;
  
  public Server zkServer, sparknginServer, genericServer ;
  public Server[] kafkaServer ;
  public Shell shell ;
  
  public SparknginClusterBuilder() {
    kafkaServer = new Server[1] ;
  }
  
  public SparknginClusterBuilder(int numOfKafkaServer) {
    kafkaServer = new Server[numOfKafkaServer] ;
  }

  public void init() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    genericServer = Server.create("-Pserver.name=generic", "-Pserver.roles=generic") ;
    zkServer = Server.create("-Pserver.name=zookeeper", "-Pserver.roles=zookeeper") ;
    
    for(int i  = 0; i < kafkaServer.length; i++) {
      int id = i + 1;
      kafkaServer[i] = Server.create("-Pserver.name=kafka" + id, "-Pserver.roles=kafka") ;
    }
    sparknginServer  = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    
    shell = new Shell() ;
    shell.getShellContext().connect();
    //Wait to make sure all the servervices are launched
    Thread.sleep(2000) ;
  }
  
  public void destroy() throws Exception {
    shell.close();
    sparknginServer.destroy() ;
    genericServer.destroy() ; 
    for(int i  = 0; i < kafkaServer.length; i++) {
      kafkaServer[i].destroy() ;
    }
    zkServer.destroy() ;
  }
 
  public void install() throws Exception {
    shell.execute(
      "module install --member-role zookeeper -Pmodule.data.drop=true -Pzk:clientPort=2181 --autostart --module Zookeeper"
    ) ;

    String kafkaReplication = kafkaServer.length >= 2 ? "2" : "1" ;
    for(int i  = 0; i < kafkaServer.length; i++) {
      int id = i + 1;
      shell.execute(
          "module install "+ 
          "  --member-name kafka" + id +
          "  --autostart" +
          "  --module Kafka" +
          "  -Pmodule.data.drop=true" +
          "  -Pkafka:broker.id=" + id +
          "  -Pkafka:port=" + (9092 + i) +
          "  -Pkafka:zookeeper.connect=127.0.0.1:2181" +
          "  -Pkafka:default.replication.factor=" + kafkaReplication +
          "  -Pkafka:controller.socket.timeout.ms=90000" +
          "  -Pkafka:controlled.shutdown.enable=true" + 
          "  -Pkafka:controlled.shutdown.max.retries=3" +
          "  -Pkafka:controlled.shutdown.retry.backoff.ms=60000"
      ) ;
    }
    shell.execute(
        "module install " + 
        "  -Pmodule.data.drop=true" +
        "  -Pkafka:zookeeper.connect=127.0.0.1:2181" +
        "  --member-role generic --autostart --module KafkaConsumer"
    ) ;
    
    shell.execute(
        "module install" +
        "  -Pmodule.data.drop=true" +
        "  -Psparkngin:http-listen-port=7080" +
        "  -Psparkngin:forwarder-class=" + NullDevMessageForwarder.class.getName() +
        "  --member-role sparkngin" +
        "  --autostart --module Sparkngin"
    ) ;
    
    shell.execute("server registration");
    Thread.sleep(1000);
  }
  
  public void uninstall() {
    shell.execute("module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin");
    shell.execute("module uninstall --member-role generic --timeout 20000   --module KafkaConsumer");
    shell.execute("module uninstall --member-role kafka --timeout 20000 --module Kafka");
    shell.execute("module uninstall --member-role zookeeper --timeout 20000 --module Zookeeper");
  }
  
  public String getKafkaConnect() {
    StringBuilder b = new StringBuilder() ;
    for(int i = 0; i < kafkaServer.length; i++) {
      if(i > 0) b.append(",") ;
      b.append("127.0.0.1:").append(9092 + i) ;
    }
    return b.toString() ;
  }
}
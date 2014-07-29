package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.util.FileUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginClusterHttpServiceUnitTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("app.config.dir", "src/app/config") ;
    System.setProperty("log4j.configuration", "file:src/app/config/log4j.properties") ;
  }
  
  static protected Server zkServer, kafkaServer, sparknginServer ;
  static protected Shell shell ;
  static String TOPIC_NAME = "metrics.consumer" ;
    
  @BeforeClass
  static public void setup() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    zkServer = Server.create("-Pserver.name=zookeeper", "-Pserver.roles=zookeeper") ;
    kafkaServer = Server.create("-Pserver.name=kafka", "-Pserver.roles=kafka") ;
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
    //Wait to make sure all the servervices are launched
    Thread.sleep(2000) ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    shell.close();
    sparknginServer.destroy() ;
    kafkaServer.destroy();
    zkServer.destroy() ;
  }
  
  @Test
  public void testSendMessage() throws Exception {
    install() ;
    int NUM_OF_MESSAGES = 10000 ;
    HttpMessageClient client = new HttpMessageClient ("127.0.0.1", 7080, 300) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      message.getHeader().setTopic(TOPIC_NAME);
      client.send(message, 5000);
    }
    client.waitAndClose(30000);
    assertEquals(0, client.getErrorCount()) ;
    shell.execute("server metric");
    uninstall(); 
  }
  
  private void install() throws InterruptedException {
    String installScript =
        "module install " + 
        " -Pmodule.data.drop=true" +
        " -Pzk:clientPort=2181" +
        " --member-role zookeeper --autostart --module Zookeeper \n" +
        
        "module install " +
        " -Pmodule.data.drop=true" +
        " -Pkafka:port=9092" +
        " --member-role kafka --autostart --module Kafka \n" +
        
        "module install " +
        " -Pmodule.data.drop=true" +
        "  --member-role kafka --autostart --module KafkaConsumer \n" +
        
        "module install " + 
        "  -Psparkngin:forwarder-class=" + NullDevMessageForwarder.class.getName() +
        "  -Psparkngin:http-listen-port=7080" +
        "  --member-role sparkngin --autostart --module Sparkngin \n" ;
    shell.executeScript(installScript);
    Thread.sleep(1000);
  }
  
  void uninstall() {
    String uninstallScript = 
        "module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin \n" +
        "module uninstall --member-role kafka --timeout 20000 --module KafkaConsumer \n" +
        "module uninstall --member-role kafka --timeout 20000 --module Kafka \n" +
        "module uninstall --member-role zookeeper --timeout 20000 --module Zookeeper";
    shell.executeScript(uninstallScript);
  }
}
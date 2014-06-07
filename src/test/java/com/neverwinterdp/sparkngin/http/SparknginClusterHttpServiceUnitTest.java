package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
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
  static String TOPIC_NAME = "sparkngin" ;
    
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
    int NUM_OF_MESSAGES = 100 ;
    DumpResponseHandler handler = new DumpResponseHandler() ;
    HttpClient client = new HttpClient ("127.0.0.1", 8080, handler) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      message.getHeader().setTopic(TOPIC_NAME);
      client.post("/message", message);
    }
    Thread.sleep(1000);
    client.close();
    assertEquals(NUM_OF_MESSAGES, handler.getCount()) ;
    uninstall(); 
  }
  
  private void install() throws InterruptedException {
    String installScript =
        "module install " + 
        " -Pmodule.data.drop=true" +
        " --member-role zookeeper --autostart --module Zookeeper \n" +
        
        "module install " +
        " -Pmodule.data.drop=true" +
        " -Pkafka.zookeeper-urls=127.0.0.1:2181" +
        " -Pkafka.consumer-report.topics=" + TOPIC_NAME +
        "  --member-role kafka --autostart --module Kafka \n" +
        
        "module install --member-role sparkngin --autostart --module Sparkngin \n" ;
    shell.executeScript(installScript);
    Thread.sleep(1000);
  }
  
  void uninstall() {
    String uninstallScript = 
        "module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin \n" +
        "module uninstall --member-role kafkar --timeout 20000 --module Kafka \n" +
        "module uninstall --member-role zookeeper --timeout 20000 --module Zookeeper";
    shell.executeScript(uninstallScript);
  }
}
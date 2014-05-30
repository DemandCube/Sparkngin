package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
import com.neverwinterdp.queuengin.kafka.cluster.KafkaServiceModule;
import com.neverwinterdp.queuengin.kafka.cluster.ZookeeperServiceModule;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.cluster.ClusterClient;
import com.neverwinterdp.server.cluster.ClusterMember;
import com.neverwinterdp.server.cluster.hazelcast.HazelcastClusterClient;
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
  static protected ClusterClient clusterClient ;
  static String TOPIC_NAME = "sparkngin" ;
    
  @BeforeClass
  static public void setup() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);

    Properties zkServerProps = new Properties() ;
    zkServerProps.put("server.group", "NeverwinterDP") ;
    zkServerProps.put("server.cluster-framework", "hazelcast") ;
    zkServerProps.put("server.roles", "master") ;
    zkServerProps.put("server.available-modules", ZookeeperServiceModule.class.getName()) ;
    zkServerProps.put("server.install-modules", ZookeeperServiceModule.class.getName()) ;
    zkServerProps.put("server.install-modules-autostart", "true") ;
    //zkServerProps.put("zookeeper.config-path", "") ;
    zkServer = Server.create(zkServerProps);
    
    Properties kafkaServerProps = new Properties() ;
    kafkaServerProps.put("server.group", "NeverwinterDP") ;
    kafkaServerProps.put("server.cluster-framework", "hazelcast") ;
    kafkaServerProps.put("server.roles", "master") ;
    kafkaServerProps.put("server.available-modules", KafkaServiceModule.class.getName()) ;
    kafkaServerProps.put("server.install-modules", KafkaServiceModule.class.getName()) ;
    kafkaServerProps.put("server.install-modules-autostart", "true") ;
    kafkaServerProps.put("kafka.zookeeper-urls", "127.0.0.1:2181") ;
    kafkaServerProps.put("kafka.consumer-report.topics", TOPIC_NAME) ;
    kafkaServer = Server.create(kafkaServerProps);
    
    Properties sparknginServerProps = new Properties() ;
    sparknginServerProps.put("server.group", "NeverwinterDP") ;
    sparknginServerProps.put("server.cluster-framework", "hazelcast") ;
    sparknginServerProps.put("server.roles", "master") ;
    sparknginServerProps.put("server.available-modules", SparknginHttpServiceModule.class.getName()) ;
    sparknginServerProps.put("server.install-modules", SparknginHttpServiceModule.class.getName()) ;
    sparknginServerProps.put("server.install-modules-autostart", "true") ;
    sparknginServerProps.put("sparkngin.sparkngin.http-listen-port", "8080") ;
    sparknginServerProps.put("sparkngin.forwarder.class", KafkaMessageForwarder.class.getName()) ;
    sparknginServerProps.put("sparkngin.forwarder.kafka-broker-list", "127.0.0.1:9092") ;
    sparknginServer = Server.create(sparknginServerProps);
    
    clusterClient = new HazelcastClusterClient() ;
    
    //Wait to make sure all the servervices are launched
    Thread.sleep(2000) ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    clusterClient.shutdown(); 
    sparknginServer.exit(0) ;
    kafkaServer.exit(0);
    zkServer.exit(0);
  }
  
  @Test
  public void testSendMessage() throws Exception {
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
  }
}
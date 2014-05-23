package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
import com.neverwinterdp.queuengin.kafka.cluster.KafkaClusterService;
import com.neverwinterdp.queuengin.kafka.cluster.KafkaConsumerTopicReportService;
import com.neverwinterdp.queuengin.kafka.cluster.ZookeeperClusterService;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.ServerBuilder;
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

    ServerBuilder zkBuilder = new ServerBuilder() ;
    zkBuilder.addService(ZookeeperClusterService.class) ;
    zkServer = zkBuilder.build();
    
    
    ServerBuilder kafkaBuilder = new ServerBuilder() ;
    kafkaBuilder.addService(KafkaClusterService.class) ;
    kafkaBuilder.
      addService(KafkaConsumerTopicReportService.class).
      setParameter("topic", TOPIC_NAME).
      setParameter("consumerGroup", TOPIC_NAME) ;
    kafkaServer = kafkaBuilder.build();
    
    ServerBuilder sparknginBuilder = new ServerBuilder() ;
    Map<String, Object> forwarderProperties = new HashMap<String, Object>() ;
    forwarderProperties.put("kafkaBrokerList", "127.0.0.1:9092") ;
    sparknginBuilder.
      addService(SparknginClusterHttpService.class).
      setParameter("listenPort", 8080).
      //setParameter("forwarder", DevNullMessageForwarder.class.getName()).
      setParameter("forwarder", KafkaMessageForwarder.class.getName()).
      setParameter("forwarderProperties", forwarderProperties) ;
    sparknginServer = sparknginBuilder.build();
    
    ClusterMember member = sparknginServer.getCluster().getMember() ;
    String connectUrl = member.getIpAddress() + ":" + member.getPort() ;
    clusterClient = new HazelcastClusterClient(connectUrl) ;
    
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
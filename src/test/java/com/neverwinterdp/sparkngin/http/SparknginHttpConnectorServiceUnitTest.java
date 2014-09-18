package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.util.FileUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpConnectorServiceUnitTest {
  static SparknginClusterBuilder clusterBuilder ;
    
  @BeforeClass
  static public void setup() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    clusterBuilder = new SparknginClusterBuilder() ;
    clusterBuilder.init(); 
    clusterBuilder.install() ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    clusterBuilder.uninstall();
    clusterBuilder.destroy();
  }
  
  @Test
  public void testSendJSONMessage() throws Exception {
    int NUM_OF_MESSAGES = 100 ;
    JSONHttpSparknginClient client = new JSONHttpSparknginClient ("127.0.0.1", 7080, 300) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      message.getHeader().setTopic(SparknginClusterBuilder.TOPIC);
      if(i % 2 == 0) {
        client.sendGet(message, 5000);
      } else {
        client.sendPost(message, 5000);
      }
    }
    client.waitAndClose(10000);
    assertEquals(0, client.getErrorCount()) ;
    clusterBuilder.shell.execute("server metric");
    client.close();
  }
  
  @Test
  public void testSendJBinary() throws Exception {
    int NUM_OF_MESSAGES = 1000 ;
    JBinaryHttpSparknginClient client = new JBinaryHttpSparknginClient("127.0.0.1", 7080, 300) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      message.getHeader().setTopic(SparknginClusterBuilder.TOPIC);
      client.sendPost(message, 5000);
    }
    client.waitAndClose(10000);
    assertEquals(0, client.getErrorCount()) ;
    clusterBuilder.shell.execute("server metric");
    client.close();
  }
}
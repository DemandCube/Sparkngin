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
public class SparknginClusterHttpServiceUnitTest {
  static SparknginClusterBuilder clusterBuilder ;
    
  @BeforeClass
  static public void setup() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    clusterBuilder = new SparknginClusterBuilder() ;
    clusterBuilder.start(); 
  }

  @AfterClass
  static public void teardown() throws Exception {
    clusterBuilder.destroy();
  }
  
  @Test
  public void testSendMessage() throws Exception {
    clusterBuilder.install() ;
    int NUM_OF_MESSAGES = 10000 ;
    HttpMessageClient client = new HttpMessageClient ("127.0.0.1", 7080, 300) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      message.getHeader().setTopic(SparknginClusterBuilder.TOPIC_NAME);
      client.send(message, 5000);
    }
    client.waitAndClose(30000);
    assertEquals(0, client.getErrorCount()) ;
    clusterBuilder.shell.execute("server metric");
    clusterBuilder.uninstall(); 
  }
}
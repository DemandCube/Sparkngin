package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpServerUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }

  private DevNullMessageForwarder forwarder ;
  private HttpServer server ;
  
  @Before
  public void setup() throws Exception {
    forwarder = new DevNullMessageForwarder() ;
    server = new HttpServer();
    server.add("/message", new MessageRouteHandler(forwarder, 10));
    server.startAsDeamon();
    Thread.sleep(2000);
  }
  
  @After
  public void teardown() {
    server.shutdown() ;
  }
  
  @Test
  public void testSendMessage() throws Exception {
    int NUM_OF_MESSAGES = 150 ;
    DumpResponseHandler handler = new DumpResponseHandler() ;
    HttpClient client = new HttpClient ("127.0.0.1", 8080, handler) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      client.post("/message", message);
    }
    Thread.sleep(1000);
    client.close() ;
    assertEquals(NUM_OF_MESSAGES, handler.getCount()) ;
    assertEquals(NUM_OF_MESSAGES, forwarder.getProcessCount()) ;
  }
}

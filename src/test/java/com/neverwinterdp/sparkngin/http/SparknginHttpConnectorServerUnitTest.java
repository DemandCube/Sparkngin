package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpConnectorServerUnitTest {

  private SparknginServer server ;
  
  @Before
  public void setup() throws Exception {
    server = new SparknginServer() ;
  }
  
  @After
  public void teardown() {
    server.shutdown() ;
  }
  
  @Test
  public void testStaticFileHandler() throws Exception {
    DumpResponseHandler handler = new DumpResponseHandler() ;
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", 7080, handler) ;
    client.get("/build.gradle");
    Thread.sleep(100) ;
  }
  
  @Test
  public void testMessageRouteHandler() throws Exception {
    int NUM_OF_MESSAGES = 5 ;
    DumpResponseHandler handler = new DumpResponseHandler() ;
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", 7080, handler) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      Message message = new Message("m" + i, "message " + i, true) ;
      client.post("/message/json", message);
    }
    long stopTime = System.currentTimeMillis() + 10000 ;
    while(System.currentTimeMillis() < stopTime && 
          server.forwarder.getProcessCount() != NUM_OF_MESSAGES) {
      Thread.sleep(100);
    }
    client.close() ;
    assertEquals(NUM_OF_MESSAGES, handler.getCount()) ;
    assertEquals(NUM_OF_MESSAGES, server.forwarder.getProcessCount()) ;
  }
}

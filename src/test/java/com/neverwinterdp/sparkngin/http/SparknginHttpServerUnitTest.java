package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
import com.neverwinterdp.netty.http.route.StaticFileHandler;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpServerUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }

  private NullDevMessageForwarder forwarder ;
  private HttpServer server ;
  private ApplicationMonitor appMonitor  ;
  
  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("build/queue", false) ;
    forwarder = new NullDevMessageForwarder() ;
    server = new HttpServer() ;
    appMonitor = new ApplicationMonitor() ;
    server.add("/message", new MessageRouteHandler(appMonitor, forwarder, "build/queue/data")) ;
    server.setDefault(new StaticFileHandler(".")) ;
    server.startAsDeamon() ;
    Thread.sleep(2000) ;
  }
  
  @After
  public void teardown() {
    server.shutdown() ;
  }
  
  @Test
  public void testStaticFileHandler() throws Exception {
    DumpResponseHandler handler = new DumpResponseHandler() ;
    HttpClient client = new HttpClient ("127.0.0.1", 8080, handler) ;
    client.get("/build.gradle");
    Thread.sleep(100) ;
  }
  
  @Test
  public void testMessageRouteHandler() throws Exception {
    int NUM_OF_MESSAGES = 5 ;
    DumpResponseHandler handler = new DumpResponseHandler() ;
    HttpClient client = new HttpClient ("127.0.0.1", 8080, handler) ;
    for(int i = 0; i < NUM_OF_MESSAGES; i++) {
      Message message = new Message("m" + i, "message " + i, true) ;
      client.post("/message", message);
    }
    long stopTime = System.currentTimeMillis() + 10000 ;
    while(System.currentTimeMillis() < stopTime && 
          forwarder.getProcessCount() != NUM_OF_MESSAGES) {
      Thread.sleep(100);
    }
    client.close() ;
    assertEquals(NUM_OF_MESSAGES, handler.getCount()) ;
    assertEquals(NUM_OF_MESSAGES, forwarder.getProcessCount()) ;
  }
}

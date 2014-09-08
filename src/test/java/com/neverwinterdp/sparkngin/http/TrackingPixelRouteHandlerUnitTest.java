package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.sparkngin.http.TrackingPixelRouteHandler;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class TrackingPixelRouteHandlerUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
    //TODO: To enable the netty leak report.
    //No idea why there is a ByteBuf memory leak. Not sure it is our bug.
    //System.setProperty("io.netty.leakDetectionLevel", "advanced") ;
  }

  private HttpServer server ;
  private NullDevMessageForwarder forwarder  ;
  
  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("build/queue", false) ;
    forwarder = new NullDevMessageForwarder() ;
    ApplicationMonitor appMonitor = new ApplicationMonitor() ;
    Sparkngin sparkngin = new Sparkngin(appMonitor, forwarder, "build/queue/data") ;
    server = new HttpServer() ;
    server.add("/pixel", new TrackingPixelRouteHandler(sparkngin)) ;
    server.startAsDeamon() ;
    Thread.sleep(2000) ;
  }
  
  @After
  public void teardown() {
    server.shutdown() ;
  }
  
  @Test
  public void testPixel() throws Exception {
    TrackingPixelResponseHandler handler = new TrackingPixelResponseHandler();
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", server.getPort(), handler) ;
    int LOOP = 10;
    
    for(int i = 0; i < LOOP; i++) {
      client.get("/pixel") ;
    }
    //Wait to make sure all the ack are return to the client
    Thread.sleep(5000);
    //Make sure testCount responses have been received
    assertEquals(LOOP, handler.getCount());
    assertEquals(LOOP, forwarder.getProcessCount());
    //Make sure no failure was caught since ResponseHandler doesn't have error handling
    //and it eats up the error without reporting a test failure
    assertEquals(0,handler.getFailure());
    System.err.println("before.............") ;
    client.close();
    System.err.println("after..............") ;
    //TODO: the client current does not support the cookie. If we want to test the cookie. Need to enable this sleep
    //method and use the browser with http://localhost:8080/pixel to test. You can use browser header inspector to check
    //the cookie.
    Thread.sleep(60000000);
  }
  
  /**
   * Handler to make sure when HTTP response is received,
   * it matches the content served from TrackingPixelRouteHandler
   */
  static public class TrackingPixelResponseHandler implements ResponseHandler {
    int count = 0; 
    int failure = 0;
    
    public void onResponse(HttpResponse response) {
      count++;
      HttpContent content = (HttpContent) response;
      ByteBuf buf = content.content();
      try {
        assertEquals(TrackingPixelRouteHandler.getImageBytes(), buf);
      } catch(AssertionError e){
        failure++;
      }
    }
    
    public int getCount(){ return this.count; }
    
    public int getFailure(){ return this.failure; }
  }
}
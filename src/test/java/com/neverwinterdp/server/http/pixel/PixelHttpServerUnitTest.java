package com.neverwinterdp.server.http.pixel;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class PixelHttpServerUnitTest {
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
    server.add("/pixel", new PixelRouteHandler(sparkngin)) ;
    server.startAsDeamon() ;
    Thread.sleep(2000) ;
  }
  
  @After
  public void teardown() {
    server.shutdown() ;
  }
  
  @Test
  public void testPixel() throws Exception {
    PixelCheckResponseHandler handler = new PixelCheckResponseHandler();
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", server.getPort(), handler) ;
    int LOOP = 1000 ;
    
    for(int i = 0; i < LOOP; i++) {
      client.get("/pixel") ;
    }
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
  }
}
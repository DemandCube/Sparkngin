package com.neverwinterdp.server.http.pixel;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.gateway.ClusterGateway;
import com.neverwinterdp.server.http.pixel.PixelRouteHandler;

/**
 * @author Richard Duarte
 */
public class HttpServerPixelRouteHandlerUnitTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static protected Server   instance ;
  static ClusterGateway gateway ;
  static int port = 8080;
  
  @BeforeClass
  static public void setup() throws Exception {
    String[] args = {
      "-Pserver.group=NeverwinterDP", "-Pserver.name=webserver", "-Pserver.roles=webserver"
    };

    instance = Server.create(args) ;
    gateway = new ClusterGateway() ;
    gateway.execute(
        "module install " +
        " -Phttp:port="+Integer.toString(port) +
        " -Phttp:route.names=pixel" +
        " -Phttp:route.pixel.handler=com.neverwinterdp.server.http.pixel.PixelRouteHandler" +
        " -Phttp:route.pixel.path=/pixel" +
        " --member-name webserver --autostart --module Http"
    ) ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    gateway.close(); 
    instance.exit(0) ;
  }
  
  @Test
  public void testContentReturnedMatchesContentServed100Requests() throws Exception {
    PixelCheckResponseHandler handler = new PixelCheckResponseHandler();
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", port, handler) ;
    int testCount = 100;
    for(int i = 0; i < testCount; i++) {
      client.get("/pixel");
    }
    Thread.sleep(1000);
    
    //Make sure testCount responses have been received
    assertEquals(testCount, handler.getCount());
    
    //Make sure no failure was caught since ResponseHandler doesn't have error handling
    //and it eats up the error without reporting a test failure
    assertEquals(0,handler.getFailure());
    
    client.close();
  }

  /**
   * Handler to make sure when HTTP response is received,
   * it matches the content served from PixelRouteHandler
   */
  static public class PixelCheckResponseHandler implements ResponseHandler {
    int count = 0; 
    int failure = 0;
    
    public void onResponse(HttpResponse response) {
      count++;
      HttpContent content = (HttpContent) response;
      ByteBuf buf = content.content();
      try{
        assertEquals(PixelRouteHandler.getImageBytes(), buf);
      }
      catch(AssertionError e){
        failure++;
        e.printStackTrace();
      }
    }
    
    public int getCount(){
      return this.count;
    }
    
    public int getFailure(){
      return this.failure;
    }
    
  }
}
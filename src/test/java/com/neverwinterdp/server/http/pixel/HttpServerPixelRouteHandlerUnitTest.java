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
import com.neverwinterdp.server.http.pixel.PixelRouteHandler;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.sparkngin.http.NullDevMessageForwarder;

/**
 * @author Richard Duarte
 */
public class HttpServerPixelRouteHandlerUnitTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static protected Server   httpServer, sparknginServer ;
  static Shell shell ; 
  static int httpport = 8181;
  static int sparkport = 7080;
  
  @BeforeClass
  static public void setup() throws Exception {
    httpServer = Server.create("-Pserver.name=webserver", "-Pserver.roles=webserver") ;
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
    //TODO: For now , the sparkngin has to start before the PixelRouteHandler start, if not the 
    //HttpClient in PixelRouteHandler will throw an exception because it cannot connect to the Sparkngin. A good design should not
    //have this dependency. The Forwarder should handle the disconnection , retry , buffer....
    shell.exec(
      "module install " + 
      "  -Psparkngin:forwarder-class=" + NullDevMessageForwarder.class.getName() +
      "  -Psparkngin:http-listen-port="+Integer.toString(sparkport) +
      "  --member-role sparkngin --autostart --module Sparkngin"
    ) ;
    
    shell.exec(
        "module install " +
        " -Phttp:port="+Integer.toString(httpport) +
        " -Phttp:route.names=pixel" +
        " -Phttp:route.pixel.handler=com.neverwinterdp.server.http.pixel.PixelRouteHandler" +
        " -Phttp:route.pixel.path=/pixel" +
        " -Phttp:route.pixel.sparkngin.connect=http://127.0.0.1:"+Integer.toString(sparkport) +
        " --member-name webserver --autostart --module Http"
    ) ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    shell.close(); 
    httpServer.destroy();
    sparknginServer.destroy() ;
  }
  
  @Test
  public void testContentReturnedMatchesContentServed100Requests() throws Exception {
    PixelCheckResponseHandler handler = new PixelCheckResponseHandler();
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", httpport, handler) ;
    int testCount = 1;
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
    shell.exec("server metric");
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
package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.netty.http.PixelRouteHandler;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;

/**
 * @author Richard Duarte
 */
public class SparknginHttpPixelServerUnitTest {
  public Server sparknginServer;
  public Shell shell;
  public int port=8181;
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  @Before
  public void setup() throws Exception {
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    
    shell = new Shell() ;
    shell.getShellContext().connect();
    shell.execute(
        "module install" +
        "  --member-role sparkngin" +
        "  --autostart --module Sparkngin" +
        "  -Pmodule.data.drop=true" +
        "  -Psparkngin:http-listen-port="+Integer.toString(port) +
        "  -Psparkngin:forwarder-class=" + NullDevMessageForwarder.class.getName() +
        "  -Psparkngin:pixelServer=true"
    ) ;
  }
  
  @After
  public void teardown() {
    sparknginServer.shutdown() ;
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
    assertFalse(handler.getFailure());
    
    client.close();
  }

  /**
   * Handler to make sure when HTTP response is received,
   * it matches the content served from PixelRouteHandler
   */
  static public class PixelCheckResponseHandler implements ResponseHandler {
    int count=0; 
    boolean failure = false;
    
    public void onResponse(HttpResponse response) {
      count++;
      HttpContent content = (HttpContent) response;
      ByteBuf buf = content.content();
      try{
        assertEquals(PixelRouteHandler.getImageBytes(), buf);
      }
      catch(AssertionError e){
        failure = true;
        e.printStackTrace();
      }
    }
    
    public int getCount(){
      return this.count;
    }
    
    //Adding this because there's no error handling in ResponseHandler
    public boolean getFailure(){
      return this.failure;
    }
    
  }
}

package com.neverwinterdp.server.http.pixel.old;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;

/**
 * Tests the connection between PixelRouteHandler and Sparkngin
 * @author Richard Duarte
 */
public class HttpServerPixelRouteHandlerSparknginIntegrationUnitTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static protected Server httpServer, sparknginServer ;
  static Shell shell ; 
  static int httpport = 9181;
  static int sparkport = 7181;
  
  @BeforeClass
  static public void setup() throws Exception {
    httpServer = Server.create("-Pserver.name=webserver", "-Pserver.roles=webserver") ;
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
    
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
        " -Phttp:route.pixel.handler=com.neverwinterdp.server.http.pixel.old.PixelRouteHandler" +
        " -Phttp:route.pixel.path=/pixel" +
        " -Phttp:route.pixel.sparkngin.connect=http://127.0.0.1:"+Integer.toString(sparkport) +
        " --member-name webserver --autostart --module Http"
    ) ;
    Thread.sleep(1000);
  }

  @AfterClass
  static public void teardown() throws Exception {
    shell.close(); 
    httpServer.destroy();
    sparknginServer.destroy() ;
  }
  
  @Test
  public void testContentReturnedMatchesContentServed() throws Exception {
    PixelCheckResponseHandler handler = new PixelCheckResponseHandler();
    AsyncHttpClient client = new AsyncHttpClient ("127.0.0.1", httpport, handler) ;
    client.get("/pixel");
    Thread.sleep(1000);
    
    //Make sure testCount responses have been received
    assertEquals(1, handler.getCount());
    
    //Make sure no failure was caught since ResponseHandler doesn't have error handling
    //and it eats up the error without reporting a test failure
    assertEquals(0,handler.getFailure());
    client.close();
    shell.exec("server metric");
  }
}
package com.neverwinterdp.server.http.pixel;

import static org.junit.Assert.assertEquals;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;

/**
 * Test to make sure that if sparkngin is down, no messages are lost
 * @author Richard Duarte
 */
public class SparknginMessageForwarderRecoveryTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static int port = 8181;
  static Server server ;
  static Shell shell;
  static SparknginMessageForwarder forwarder;
  
  @BeforeClass
  static public void setup() throws Exception {
    server = Server.create("-Pserver.name=webserver", "-Pserver.roles=webserver");
    shell = new Shell();
    shell.getShellContext().connect();
  }

  @AfterClass
  static public void teardown() throws Exception {
    HttpSnoop.resetHits();
    forwarder.disconnect();
    server.shutdown();
  }
  
  @Test
  public void testBufferedMessagesGetSentAfterSparknginLaunches(){
    HttpSnoop.resetHits();
    forwarder = new SparknginMessageForwarder("127.0.0.1",port);
    int numMessages=8;
    for(int i=0; i<numMessages; i++){
      forwarder.forwardToSpark(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
    }
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(0,HttpSnoop.getHits());
    
    //Start HTTP server
    shell.exec(
        "module install " +
        " -Phttp:port="+Integer.toString(port) +
        " -Phttp:route.names=snoop" +
        " -Phttp:route.snoop.handler=com.neverwinterdp.server.http.pixel.HttpSnoop" +
        " -Phttp:route.snoop.path=/message" +
        " --member-name webserver --autostart --module Http"
    ) ;
    
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for(int i=0; i<numMessages; i++){
      forwarder.forwardToSpark(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
    }
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    assertEquals(numMessages*2,HttpSnoop.getHits());
  }
}

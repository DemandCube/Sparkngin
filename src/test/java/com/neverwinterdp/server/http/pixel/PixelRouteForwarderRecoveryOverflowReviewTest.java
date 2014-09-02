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
 * Sends too many messages for the buffer to handle, makes sure there are no failures and the 
 * correct number of messages are sent
 * @author Richard Duarte
 */
public class PixelRouteForwarderRecoveryOverflowReviewTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static int port = 9199;
  static Server server ;
  static Shell shell;
  static PixelLogForwarder forwarder;
  
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
    forwarder = new PixelLogForwarder("127.0.0.1",port);
    int numMessages=35000;
    for(int i=0; i<numMessages; i++){
      forwarder.forward(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
    }
    assertEquals(0,HttpSnoop.getHits());
    try {
      Thread.sleep(8000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //Start HTTP server
    shell.exec(
        "module install " +
        " -Phttp:port="+Integer.toString(port) +
        " -Phttp:route.names=snoop" +
        " -Phttp:route.snoop.handler=com.neverwinterdp.server.http.pixel.HttpSnoop" +
        " -Phttp:route.snoop.path=/message" +
        " --member-name webserver --autostart --module Http"
    ) ;

    //Could take up to 6 seconds to reconnect
    //Plus need time to allow buffer to catch up
    try {
      Thread.sleep(8000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    //Make sure only the size of the buffer is sent
    assertEquals(10000,HttpSnoop.getHits());
    for(int i=0; i<10000; i++) {
      forwarder.forward(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
    }
    
    //Give queue chance to catch up again
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    //Queue is size 100, plus the next 100 messages, make sure they're all seen
    assertEquals(20000,HttpSnoop.getHits());
  }
}

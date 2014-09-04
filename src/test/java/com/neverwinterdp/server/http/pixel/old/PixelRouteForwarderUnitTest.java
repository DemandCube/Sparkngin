package com.neverwinterdp.server.http.pixel.old;

import static org.junit.Assert.assertEquals;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.http.pixel.RequestLog;
import com.neverwinterdp.server.http.pixel.old.PixelLogForwarder;
import com.neverwinterdp.server.shell.Shell;

/**
 * Test that messages are being send with SparknginMessageForwarder
 * @author Richard Duarte
 *
 */
public class PixelRouteForwarderUnitTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }
  
  static int port = 9999;
  static Server server ;
  static Shell shell;
  static PixelLogForwarder forwarder;
  
  @BeforeClass
  static public void setup() throws Exception {
    server = Server.create("-Pserver.name=webserver", "-Pserver.roles=webserver") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
    
    
    shell.exec(
        "module install " +
        " -Phttp:port="+Integer.toString(port) +
        " -Phttp:route.names=snoop" +
        " -Phttp:route.snoop.handler=com.neverwinterdp.server.http.pixel.HttpSnoop" +
        " -Phttp:route.snoop.path=/message" +
        " --member-name webserver --autostart --module Http"
    ) ;
    Thread.sleep(3000);
  }

  @AfterClass
  static public void teardown() throws Exception {
    server.shutdown() ;
    HttpSnoop.resetHits();
    forwarder.disconnect();
  }
  
  @Test
  public void testMessageForwarderHitsHttpPost() {
    HttpSnoop.resetHits();
    forwarder = new PixelLogForwarder("127.0.0.1",port);
    forwarder.forward(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
    //Give the buffer a chance to catch up
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(1,HttpSnoop.getHits());
  }
}

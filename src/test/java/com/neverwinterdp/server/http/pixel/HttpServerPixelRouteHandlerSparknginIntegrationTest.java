package com.neverwinterdp.server.http.pixel;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.shell.Shell;
import com.neverwinterdp.sparkngin.http.NullDevMessageForwarder;
import com.neverwinterdp.util.FileUtil;
/**
 * @author Richard Duarte
 */
public class HttpServerPixelRouteHandlerSparknginIntegrationTest {
  static {
    System.setProperty("app.dir", "build/cluster") ;
    System.setProperty("app.config.dir", "src/app/config") ;
    System.setProperty("log4j.configuration", "file:src/app/config/log4j.properties") ;
  }
  
  static int sparkPort = 7080;
  static int pixPort = 8181;
  static protected Server pixelServer, sparknginServer ;
  static protected Shell shell ;
  static String TOPIC_NAME = "metrics.consumer" ;
    
  @BeforeClass
  static public void setup() throws Exception {
    FileUtil.removeIfExist("build/cluster", false);
    pixelServer = Server.create("-Pserver.name=pixel", "-Pserver.roles=webserver");
    sparknginServer = Server.create("-Pserver.name=sparkngin", "-Pserver.roles=sparkngin") ;
    shell = new Shell() ;
    shell.getShellContext().connect();
    //Wait to make sure all the servervices are launched
    Thread.sleep(2000) ;
  }

  @AfterClass
  static public void teardown() throws Exception {
    shell.close();
    sparknginServer.destroy() ;
  }
  

  
  private void install() throws InterruptedException {
    String installScript =
        "module install " + 
        "  -Psparkngin:forwarder-class=" + NullDevMessageForwarder.class.getName() +
        "  -Psparkngin:http-listen-port="+Integer.toString(sparkPort) +
        "  --member-role sparkngin --autostart --module Sparkngin \n" +

        "module install " +
        " -Phttp:port="+Integer.toString(pixPort) +
        " -Phttp:route.names=pixel" +
        " -Phttp:route.pixel.handler=com.neverwinterdp.server.http.pixel.PixelRouteHandler" +
        " -Phttp:route.pixel.path=/pixel" +
        " --member-name webserver --autostart --module Http";

    shell.executeScript(installScript);
    Thread.sleep(1000);
  }
  
  void uninstall() {
    String uninstallScript = 
        "module uninstall --member-role sparkngin --timeout 20000 --module Sparkngin \n"+
        "module uninstall --member-role webserver --timeout 20000 --module Sparkngin \n" ;
    shell.executeScript(uninstallScript);
  }
}
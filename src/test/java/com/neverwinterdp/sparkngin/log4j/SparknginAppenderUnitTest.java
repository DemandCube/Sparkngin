package com.neverwinterdp.sparkngin.log4j;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.sparkngin.http.JSONMessageRouteHandler;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.yara.MetricRegistry;

public class SparknginAppenderUnitTest {
  static {
    System.setProperty("log4j.configuration", "file:src/test/resources/log4j.properties") ;
  }

  NullDevMessageForwarder forwarder ;
  Sparkngin sparkngin ;
  HttpServer server ;
  MetricRegistry metricRegistry  ;
  
  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("build/queue", false) ;
    forwarder = new NullDevMessageForwarder() ;
    server = new HttpServer() ;
    server.setPort(7080) ;
    metricRegistry = new MetricRegistry() ;
    sparkngin = new Sparkngin(metricRegistry, forwarder, "build/queue/data") ;
    server.add("/message/json", new JSONMessageRouteHandler(sparkngin)) ;
    server.startAsDeamon() ;
  }
  
  @After
  public void shutdown() {
    server.shutdown() ;
    sparkngin.close();
  }
  
  @Test
  public void testAppender() throws Exception {
    Logger logger = server.getLoggerFactory().getLogger("TEST") ;
    for(int i = 0; i < 10; i++) {
      Thread.sleep(1000);
      logger.info("this is a test...................");
    }
    System.out.println("forward: " + forwarder.getProcessCount());
    Assert.assertTrue(forwarder.getProcessCount() > 0);
  }
}

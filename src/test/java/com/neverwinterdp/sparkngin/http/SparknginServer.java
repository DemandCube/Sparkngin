package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.StaticFileHandler;
import com.neverwinterdp.sparkngin.NullDevMessageForwarder;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.yara.MetricRegistry;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginServer {
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties") ;
  }

  NullDevMessageForwarder forwarder ;
  HttpServer server ;
  MetricRegistry metricRegistry  ;
  
  public SparknginServer() throws Exception {
    FileUtil.removeIfExist("build/queue", false) ;
    forwarder = new NullDevMessageForwarder() ;
    server = new HttpServer() ;
    server.setPort(7080) ;
    metricRegistry = new MetricRegistry() ;
    server.add("/message/json", new JSONMessageRouteHandler(new Sparkngin(metricRegistry, forwarder, "build/queue/data"))) ;
    server.setDefault(new StaticFileHandler(".")) ;
    server.startAsDeamon() ;
    Thread.sleep(2000) ;
  }
  
  public void shutdown() {
    server.shutdown() ;
  }
}

package com.neverwinterdp.sparkngin.http;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.StaticFileHandler;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.LoggerFactory;

public class SparknginHttpService extends AbstractService {
  private LoggerFactory loggerFactory ;
  private Logger logger ;
  private HttpServer server ;
  
  @Inject
  private Sparkngin sparkngin ;
  
  private SparknginClusterHttpServiceInfo serviceInfo ;
  
  @Inject
  public void init(Injector container,
                   LoggerFactory factory, 
                   ModuleProperties moduleProperties,
                   SparknginClusterHttpServiceInfo serviceInfo) throws Exception {
    this.loggerFactory = factory ;
    logger = factory.getLogger(SparknginHttpService.class) ;
    this.serviceInfo = serviceInfo ;
    if(moduleProperties.isDataDrop()) cleanup() ;
  }
  
  public boolean cleanup() throws Exception {
    sparkngin.cleanup() ;
    logger.info("Clean queue data directory");
    return true ;
  }
  
  public void start() throws Exception {
    logger.info("Start start()");
    logger.info("Properties:\n" + JSONSerializer.INSTANCE.toString(serviceInfo)) ;
    server = new HttpServer() ;
    server.setPort(serviceInfo.getHttpListenPort()) ;
    server.setLoggerFactory(loggerFactory) ;
    if(serviceInfo.getWwwDir() != null) {
      StaticFileHandler fileHandler = new StaticFileHandler(serviceInfo.getWwwDir()) ;
      fileHandler.setLogger(loggerFactory.getLogger(StaticFileHandler.class)) ;
      server.setDefault(fileHandler) ;
    }
    
    server.add("/message", new MessageRouteHandler(sparkngin));
    server.add("/tracking/site/:site", new TrackingPixelRouteHandler(sparkngin));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop() hashcode = " + hashCode());
    server.shutdown();
    logger.info("Finish stop()");
  }
}
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

public class SparknginHttpConnectorService extends AbstractService {
  private LoggerFactory loggerFactory ;
  private Logger logger ;
  private HttpServer server ;
  
  @Inject
  private Sparkngin sparkngin ;
  
  private SparknginHttpConnectorServiceInfo serviceInfo ;
  
  @Inject
  public void init(Injector container,
                   LoggerFactory factory, 
                   ModuleProperties moduleProperties,
                   SparknginHttpConnectorServiceInfo serviceInfo) throws Exception {
    this.loggerFactory = factory ;
    logger = factory.getLogger(SparknginHttpConnectorService.class) ;
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
    server.setPort(serviceInfo.httpListenPort()) ;
    server.setLoggerFactory(loggerFactory) ;
    if(serviceInfo.wwwDir() != null) {
      StaticFileHandler fileHandler = new StaticFileHandler(serviceInfo.wwwDir()) ;
      fileHandler.setLogger(loggerFactory.getLogger(StaticFileHandler.class)) ;
      server.setDefault(fileHandler) ;
    }
    
    server.add("/message/json", new JSONMessageRouteHandler(sparkngin));
    server.add("/message/jbinary", new JBinaryMessageRouteHandler(sparkngin));
    server.add("/tracker/:trackerName/:site", new TrackingPixelRouteHandler(sparkngin, serviceInfo.getProperties()));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop() hashcode = " + hashCode());
    server.shutdown();
    logger.info("Finish stop()");
  }
}
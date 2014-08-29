package com.neverwinterdp.sparkngin.http;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.server.http.pixel.PixelRouteHandler;
import com.neverwinterdp.netty.http.StaticFileHandler;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.util.monitor.ApplicationMonitor;

public class SparknginClusterHttpService extends AbstractService {
  private LoggerFactory loggerFactory ;
  private Logger logger ;
  private HttpServer server ;
  
  @Inject
  private ApplicationMonitor appMonitor ;
  
  private MessageForwarder messageForwarder ;
  
  private SparknginClusterHttpServiceInfo serviceInfo ;
  
  @Inject
  public void init(Injector container,
                   LoggerFactory factory, 
                   ModuleProperties moduleProperties,
                   SparknginClusterHttpServiceInfo serviceInfo) throws Exception {
    this.loggerFactory = factory ;
    logger = factory.getLogger(SparknginClusterHttpService.class) ;
    this.serviceInfo = serviceInfo ;
    if(moduleProperties.isDataDrop()) cleanup() ;
    
    Class<MessageForwarder> type = (Class<MessageForwarder>) Class.forName(serviceInfo.getForwarderClass()) ;
    messageForwarder = container.getInstance(type) ;
  }
  
  public boolean cleanup() throws Exception {
    FileUtil.removeIfExist(serviceInfo.getQueueDir(), false);
    logger.info("Clean queue data directory");
    return true ;
  }
  
  public void start() throws Exception {
    logger.info("Start start()");
    logger.info("Properties: \n" + JSONSerializer.INSTANCE.toString(serviceInfo)) ;
    
    server = new HttpServer();
    server.setPort(serviceInfo.getHttpListenPort()) ;
    server.setLoggerFactory(loggerFactory) ;
    if(serviceInfo.getWwwDir() != null) {
      StaticFileHandler fileHandler = new StaticFileHandler(serviceInfo.getWwwDir()) ;
      fileHandler.setLogger(loggerFactory.getLogger(StaticFileHandler.class)) ;
      server.setDefault(fileHandler) ;
    }
    server.add("/message", new MessageRouteHandler(appMonitor, messageForwarder, serviceInfo.getQueueDir()));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop() hashcode = " + hashCode());
    server.shutdown();
    logger.info("Finish stop()");
  }
}
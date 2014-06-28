package com.neverwinterdp.sparkngin.http;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.util.BeanInspector;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.util.monitor.ApplicationMonitor;

public class SparknginClusterHttpService extends AbstractService {
  private LoggerFactory loggerFactory ;
  private Logger logger ;
  private HttpServer server ;
  
  @Inject
  private ModuleProperties moduleProperties; 
  
  @Inject
  private ApplicationMonitor appMonitor ;
  
  private MessageForwarder messageForwarder ;
  
  @Inject(optional = true) @Named("http-listen-port")
  private int httpListenPort = 8080;
  
  @Inject
  public void init(LoggerFactory factory) {
    this.loggerFactory = factory ;
    logger = factory.getLogger(getClass().getSimpleName()) ;
  }
  
  @Inject
  public void init(Injector container, @Named("forwarder-class") String forwarderClass) throws Exception {
    Class<MessageForwarder> type = (Class<MessageForwarder>) Class.forName(forwarderClass) ;
    messageForwarder = container.getInstance(type) ;
    messageForwarder.onInit() ;
  }
  
  public void start() throws Exception {
    logger.info("Start start()");
    logger.info("http-listen-port = " + httpListenPort) ;
    logger.info("forwarder-class = " + messageForwarder.getClass()) ;
    String queueDir = moduleProperties.getDataDir() + "/sparkngin/queue" ;
    logger.info("queue dir = " + queueDir) ;
    
    server = new HttpServer();
    server.setPort(httpListenPort) ;
    server.setLoggerFactory(loggerFactory) ;
    server.add("/message", new MessageRouteHandler(appMonitor, messageForwarder, queueDir));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop() hashcode = " + hashCode());
    server.shutdown();
    logger.info("Finish stop()");
  }
}
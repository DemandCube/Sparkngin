package com.neverwinterdp.sparkngin.http;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.util.BeanInspector;
import com.neverwinterdp.util.LoggerFactory;

public class SparknginClusterHttpService extends AbstractService {
  private LoggerFactory loggerFactory ;
  private Logger logger ;
  private HttpServer server ;
  
  @Inject(optional = true) @Named("sparkngin.forwarder.class")
  private String forwarderClass = NullDevMessageForwarder.class.getName() ;
  
  @Inject(optional = true) @Named("sparkngin.http-listen-port")
  private int httpListenPort = 8080;
  
  @Inject(optional = true) @Named("sparkngin.queue-buffer")
  private int queueBuffer = 1000;
  
  @Inject
  public void init(LoggerFactory factory) {
    this.loggerFactory = factory ;
    logger = factory.getLogger(getClass().getSimpleName()) ;
  }
  
  public void start() throws Exception {
    logger.info("Start start()");
    Class<?> forwarderType = Class.forName(forwarderClass) ;
    BeanInspector<MessageForwarder> fInspector = new BeanInspector(forwarderType) ;
    MessageForwarder forwarder = fInspector.newInstance() ;
    forwarder.onInit(); 
    server = new HttpServer();
    server.setPort(httpListenPort) ;
    server.setLoggerFactory(loggerFactory) ;
    server.add("/message", new MessageRouteHandler(forwarder, queueBuffer));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop()");
    server.shutdown();
    logger.info("Finish stop()");
  }
}

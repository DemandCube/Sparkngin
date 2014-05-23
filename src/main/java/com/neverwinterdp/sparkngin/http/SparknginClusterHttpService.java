package com.neverwinterdp.sparkngin.http;

import java.util.Map;

import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.server.Server;
import com.neverwinterdp.server.config.ServiceConfig;
import com.neverwinterdp.server.service.AbstractService;
import com.neverwinterdp.util.BeanInspector;
import com.neverwinterdp.util.LoggerFactory;

public class SparknginClusterHttpService extends AbstractService {
  private HttpServer server ;
  private LoggerFactory loggerFactory  ;
  
  public void onInit(Server server) {
    super.onInit(server);
    loggerFactory = server.getLoggerFactory() ;
  }
  
  public void start() throws Exception {
    logger.info("Start start()");
    ServiceConfig config = getServiceConfig() ;
    String forawarderCLass = config.getParameter("forwarder", DevNullMessageForwarder.class.getName()) ;
    Map<String, Object> forwarderProperties = config.getParameter("forwarderProperties", (Map<String, Object>)null) ;
    Class<?> forwarderType = Class.forName(forawarderCLass) ;
    BeanInspector<MessageForwarder> fInspector = new BeanInspector(forwarderType) ;
    MessageForwarder forwarder = fInspector.newInstance(forwarderProperties) ;
    forwarder.onInit(); 
    server = new HttpServer();
    server.setPort(config.getParameter("listenPort", 8080)) ;
    server.setLoggerFactory(loggerFactory) ;
    server.add("/message", new MessageRouteHandler(forwarder, 200));
    server.startAsDeamon();
    logger.info("Finish start()");
  }

  public void stop() {
    logger.info("Start stop()");
    server.shutdown();
    logger.info("Finish stop()");
  }
}

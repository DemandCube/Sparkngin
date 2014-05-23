package com.neverwinterdp.sparkngin.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HttpServerVerticle extends Verticle {
  final static public String REPLY_MESSAGE = "pong!" ;
  static Logger logger = LoggerFactory.getLogger(Main.class);
  
  public void start() {
    JsonObject config = container.config();
    int listenPort = config.getInteger("http-listen-port") ;
    if(listenPort == 0) listenPort = 8080 ;    
    RouteMatcher matcher = new RouteMatcher();
    new MessageHandlers().configure(matcher, config);
    final HttpServer server = getVertx().createHttpServer();
    server.requestHandler(matcher);
    server.listen(listenPort);
    logger.info("HTTP Server started on " + listenPort);
  }
}
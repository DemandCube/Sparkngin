package com.neverwinterdp.sparkngin.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.json.JsonObject;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class Main {
  static {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
  }
  static Logger logger = LoggerFactory.getLogger(Main.class);
  
  static String getValue(String arg) {
    String[] array = arg.split("=", 2) ;
    return array[1] ;
  }
  
  static public void main(String[] args) throws Exception {
    System.out.println("logger = " + logger.getClass());
    String brokerList = "127.0.0.1:9092" ;
    int listenPort = 8080 ;

    logger.info("Available options: ");
    logger.info("  --listen-port=8080");
    logger.info("  --broker-list=127.0.0.1:9092");
    if(args != null) {
      for(String arg : args) {
        if(arg.startsWith("--listen-port")) listenPort = Integer.parseInt(getValue(arg)) ;
        else if(arg.startsWith("--broker-list")) brokerList = getValue(arg) ;
        else {
          logger.error("Unknown option: " + arg);
          return ;
        }
      }
    }
    
    EmbbededVertxServer server = new EmbbededVertxServer() ; 
    JsonObject config = new JsonObject() ;
    config.putNumber("http-listen-port", new Integer(listenPort)) ;
    config.putString("broker-list", brokerList) ;
    server.deployVerticle(HttpServerVerticle.class, config, 3);
    Thread.currentThread().join() ; 
  }
}
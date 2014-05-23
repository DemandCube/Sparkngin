package com.neverwinterdp.sparkngin.vertx.impl;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class EmbbededVertxServer {
  static Logger logger = LoggerFactory.getLogger(Main.class);
  
  private PlatformManager pm ;
  private URL[] classpathURL ;
  
  public EmbbededVertxServer() throws Exception {
    pm = PlatformLocator.factory.createPlatformManager();
    String cps = System.getProperty("java.class.path"); 
    String[] cp = cps.split(File.pathSeparator) ;
    classpathURL = new URL[cp.length] ;
    for(int i = 0; i < classpathURL.length; i++) {
      classpathURL[i] = new URL("file:" + cp[i].replace('\\', '/')) ;
    }
  }
  
  public PlatformManager getPlatformManager() { return this.pm ; }
  
  public void deployVerticle(Class<?> type, int numOfInstances) {
    deployVerticle(type.getName(), numOfInstances) ;
  }
  
  public void deployVerticle(Class<?> type, JsonObject conf, int numOfInstances) {
    deployVerticle(type.getName(), conf, numOfInstances) ;
  }
  
  public void deployVerticle(final String className, int numOfInstances) {
    JsonObject conf = new JsonObject();
    deployVerticle(className, conf, numOfInstances) ;
  }
  
  public void deployVerticle(final String className, JsonObject conf, int numOfInstances) {
    pm.deployVerticle(className, conf, classpathURL, numOfInstances, null, new Handler<AsyncResult<String>>() {
      public void handle(AsyncResult<String> event) {
        if (event.succeeded()) {
          logger.info("Deployed " + className + ", ID = " + event.result());
        } else {
          event.cause().printStackTrace();
        }
      }
    });
  }
  
  public void stop() {
    pm.stop(); 
  }
}

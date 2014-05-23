package com.neverwinterdp.sparkngin.vertx;

import org.junit.Assert;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import com.neverwinterdp.sparkngin.vertx.impl.EmbbededVertxServer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class EventBusPingUnitTest {
  @Test
  public void testEventBusPingVerticle() throws Exception {
    EmbbededVertxServer server = new EmbbededVertxServer() ;
    server.deployVerticle(EventBusPingVerticle.class, 1);
    Thread.sleep(1000);
    
    Vertx vertx = server.getPlatformManager().vertx() ; 
    //vertx.eventBus().publish("ping", "ping!") ;
    vertx.eventBus().send("/ping", "ping!", new Handler<Message<String>>() {
      public void handle(Message<String> reply) {
        Assert.assertEquals(EventBusPingVerticle.REPLY_MESSAGE, reply.body());
        System.out.println("Received reply: " + reply.body());
      }
    });
    System.out.println("done!!!!!!!!!!") ;
    Thread.sleep(1000) ;
  }
  

  
  static public class EventBusPingVerticle extends Verticle {
    final static public String REPLY_MESSAGE = "pong!" ;
    
    public void start() {
      vertx.eventBus().registerHandler("ping", new Handler<Message<String>>() {
        @Override
        public void handle(Message<String> message) {
          message.reply(REPLY_MESSAGE);
          System.out.println("Sent back " + REPLY_MESSAGE);
        }
      });
      container.logger().info("PingVerticle started");
    }
  }
}

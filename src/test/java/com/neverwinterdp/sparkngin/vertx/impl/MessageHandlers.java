package com.neverwinterdp.sparkngin.vertx.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MessageHandlers  {
  private KafkaMessageProducer producer ;
  
  public void configure(RouteMatcher matcher, JsonObject config) {
    String brokerList = config.getString("broker-list") ;
    if(brokerList == null) brokerList = "127.0.0.1:9090,127.0.0.1:9091" ;
    ApplicationMonitor appMonitor = new ApplicationMonitor() ;
    ComponentMonitor monitor = appMonitor.createComponentMonitor(KafkaMessageProducer.class) ;
    producer = new KafkaMessageProducer(monitor, brokerList) ;
    matcher.post("/message/:topic", new Post()) ;
  }
  
  public class Post implements Handler<HttpServerRequest> {
    public void handle(final HttpServerRequest req) {
      req.response().setStatusCode(200);
      req.response().putHeader("Content-Type", "application/json");
      req.response().setChunked(true) ;
      final String topic = req.params().get("topic") ;
      req.bodyHandler(new Handler<Buffer>() {
        public void handle(Buffer buf) {
          SendAck ack = new SendAck() ;
          byte[] bytes = buf.getBytes() ;
          try {
            Message message = JSONSerializer.INSTANCE.fromBytes(bytes, Message.class) ;
            if(message.getHeader().isTraceEnable()) {
              int port = req.localAddress().getPort() ;
              String addr = req.localAddress().getHostString() ;
              message.addTrace("JSONMessageServlet", "forward by http server, ip = " + addr + ", port " + port) ;
            }
            producer.send(topic, message) ;
            ack.setStatus(SendAck.Status.OK) ;
          } catch (Exception e) {
            ack.setStatus(SendAck.Status.ERROR) ;
            ack.setMessage(e.getMessage()) ;
          }
          req.response().end(JSONSerializer.INSTANCE.toString(ack));
        }
      });
    }
  }
}

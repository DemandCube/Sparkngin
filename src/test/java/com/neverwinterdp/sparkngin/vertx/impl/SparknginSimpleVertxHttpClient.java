package com.neverwinterdp.sparkngin.vertx.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.sparkngin.SendMessageHandler;
import com.neverwinterdp.sparkngin.SparknginSimpleClient;
import com.neverwinterdp.util.JSONSerializer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginSimpleVertxHttpClient implements SparknginSimpleClient {
  private String host ;
  private int    port ;
  private String connectionUrl ;
  private HttpClient client ;
  
  public SparknginSimpleVertxHttpClient(String connectionUrl) {
    this.connectionUrl = connectionUrl ;
    if(connectionUrl.startsWith("http://")) {
      connectionUrl = connectionUrl.substring(7) ;
    }
    String[] part = connectionUrl.split(":") ;
    this.host = part[0] ;
    this.port = Integer.parseInt(part[1]) ;
    
    Vertx vertx = VertxFactory.newVertx() ;
    client = vertx.createHttpClient() ;
    client.setHost(host).setPort(port) ;
  }
  
  public String getConnectionUrl() { return this.connectionUrl ; }
  
  public void send(String topic, final Message message, final SendMessageHandler mHandler) {
    HttpClientRequest postReq = client.post("/message/" + topic, new Handler<HttpClientResponse>() {
      public void handle(HttpClientResponse event) {
        event.bodyHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            String json = data.toString() ;
            SendAck ack = JSONSerializer.INSTANCE.fromString(json, SendAck.class) ;
            mHandler.onResponse(message, SparknginSimpleVertxHttpClient.this, ack) ;
          }
        });
      }
    }) ;
    postReq.exceptionHandler(new Handler<Throwable>() {
      public void handle(Throwable event) {
        mHandler.onError(message, SparknginSimpleVertxHttpClient.this, event) ;
      }
    }) ;
    String json = JSONSerializer.INSTANCE.toString(message);
    postReq.end(json) ;
  }
  
  static public SparknginSimpleClient[] create(String[] connectionUrls) {
    SparknginSimpleClient[] instances = new SparknginSimpleClient[connectionUrls.length] ;
    for(int i = 0; i < instances.length; i++) {
      instances[i] = new SparknginSimpleVertxHttpClient(connectionUrls[i]) ;
    }
    return instances; 
  }
}
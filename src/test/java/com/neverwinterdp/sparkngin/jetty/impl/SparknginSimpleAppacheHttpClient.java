package com.neverwinterdp.sparkngin.jetty.impl;

import java.io.ByteArrayOutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.sparkngin.SendMessageHandler;
import com.neverwinterdp.sparkngin.SparknginSimpleClient;
import com.neverwinterdp.util.JSONSerializer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginSimpleAppacheHttpClient implements SparknginSimpleClient {
  private String connectionUrl ;
  HttpClient httpClient ;
  
  public SparknginSimpleAppacheHttpClient(String connectionUrl) {
    this.connectionUrl = connectionUrl ;
    
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create() ;
    httpClientBuilder.setMaxConnTotal(10) ;
    httpClientBuilder.setMaxConnPerRoute(1) ;
    httpClient = httpClientBuilder.build();
  }
  
  public String getConnectionUrl() { return this.connectionUrl ; }
  
  public void send(String topic, Message message, SendMessageHandler handler) {
    try {
      SendAck ack = send(topic, message) ;
      handler.onResponse(message, this, ack);
    } catch (Exception error) {
      handler.onError(message, this, error);
    }
  }

  SendAck send(String topic, Message message) throws Exception {
    String url = connectionUrl + "/message/" + topic ;  
    HttpPost postRequest = new HttpPost(url);
    String json = JSONSerializer.INSTANCE.toString(message);
    StringEntity input = new StringEntity(json);
    input.setContentType("application/json");
    postRequest.setEntity(input);
    HttpResponse response = null ;
    try {
      response = httpClient.execute(postRequest);
    } finally {
      postRequest.abort();
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream() ;
    response.getEntity().writeTo(out) ;
    SendAck ack = JSONSerializer.INSTANCE.fromBytes(out.toByteArray(), SendAck.class) ;
    return ack ;
  }
  
  static public SparknginSimpleClient[] create(String[] connectionUrls) {
    SparknginSimpleClient[] instances = new SparknginSimpleClient[connectionUrls.length] ;
    for(int i = 0; i < instances.length; i++) {
      instances[i] = new SparknginSimpleAppacheHttpClient(connectionUrls[i]) ;
    }
    return instances; 
  }
}
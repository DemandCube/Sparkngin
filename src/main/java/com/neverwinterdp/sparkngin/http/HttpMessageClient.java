package com.neverwinterdp.sparkngin.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.HttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.util.JSONSerializer;

public class HttpMessageClient {
  private HttpClient client ;
  private LinkedHashMap<String, Message> messages ;
  private int bufferSize ;
  private int errorCount ;
  
  public HttpMessageClient(String host, int port, int bufferSize) throws Exception {
    client = new HttpClient (host, port, new MessageResponseHandler()) ;
    this.bufferSize = bufferSize ;
    messages = new LinkedHashMap<String, Message>() ;
  }
  
  public int getErrorCount() { return errorCount ;}
  
  public void send(Message message, long timeout) throws Exception {
    synchronized(messages) {
      if(messages.size() >= bufferSize) {
        messages.wait(timeout);
        if(messages.size() >= bufferSize) {
          throw new TimeoutException("fail to send the message in " + timeout + "ms") ;
        }
      }
      client.post("/message", message);
      String messageId = message.getHeader().getKey() ;
      messages.put(messageId, message) ;
    }
  }
  
  public void onFailedMessage(SendAck ack, Message message) {
    errorCount++ ;
    System.out.println("Failed message: " + ack.getMessageId() + ", message = " + ack.getMessage());
  }
  
  public Map<String, Message> getWaitingMessages() { 
    return this.messages ;
  }
  
  public void waitAndClose(long waitTime) throws InterruptedException {
    if(messages.size() > 0) { 
      synchronized(messages) {
        long stopTime = System.currentTimeMillis() + waitTime ;
        while(stopTime > System.currentTimeMillis() && messages.size() > 0) {
          long timeToWait = stopTime - System.currentTimeMillis() ;
          messages.wait(timeToWait);
        }
      }
    }
    client.close();
  }
  
  public void close() {
    if(messages.size() > 0) {
      throw new RuntimeException("There are " + messages.size() + " messages waitting for ack") ;
    }
    client.close(); 
  }
  
  class MessageResponseHandler implements ResponseHandler {
    public void onResponse(HttpResponse response) {
      if(response instanceof HttpContent) {
        HttpContent content = (HttpContent) response;
        String json = content.content().toString(CharsetUtil.UTF_8);
        SendAck ack = JSONSerializer.INSTANCE.fromString(json, SendAck.class) ;
        String messageId = (String) ack.getMessageId() ;
        Message message = messages.get(messageId) ;
        if(!SendAck.Status.OK.equals(ack.getStatus())) {
          onFailedMessage(ack, message) ;
        }
        synchronized(messages) {
          messages.remove(messageId) ;
          messages.notify() ;
        }
      }
    }
  }
}

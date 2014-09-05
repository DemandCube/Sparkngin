package com.neverwinterdp.sparkngin.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.util.JSONSerializer;

public class HttpSparknginClient {
  private AsyncHttpClient client ;
  private LinkedHashMap<String, Message> waitingAckMessages ;
  private int bufferSize ;
  private int sendCount = 0;
  private int errorCount = 0;
  
  public HttpSparknginClient(String host, int port, int bufferSize) throws Exception {
    client = new AsyncHttpClient (host, port, new MessageResponseHandler()) ;
    this.bufferSize = bufferSize ;
    waitingAckMessages = new LinkedHashMap<String, Message>() ;
  }
  
  public int getSendCount() { return this.sendCount ; }
  
  public int getErrorCount() { return errorCount ;}
  
  public void send(Message message, long timeout) throws Exception {
    synchronized(waitingAckMessages) {
      if(waitingAckMessages.size() >= bufferSize) {
        waitingAckMessages.wait(timeout);
        if(waitingAckMessages.size() >= bufferSize) {
          throw new TimeoutException("fail to send the message in " + timeout + "ms") ;
        }
      }
      client.post("/message", message);
      sendCount++ ;
      String messageId = message.getHeader().getKey() ;
      waitingAckMessages.put(messageId, message) ;
    }
  }
  
  public void onFailedMessage(Ack ack, Message message) {
    errorCount++ ;
    System.out.println("Failed message: " + ack.getMessageId() + ", message = " + ack.getMessage());
  }
  
  public Map<String, Message> getWaitingMessages() { 
    return this.waitingAckMessages ;
  }
  
  public void waitAndClose(long waitTime) throws InterruptedException {
    if(waitingAckMessages.size() > 0) { 
      synchronized(waitingAckMessages) {
        long stopTime = System.currentTimeMillis() + waitTime ;
        while(stopTime > System.currentTimeMillis() && waitingAckMessages.size() > 0) {
          long timeToWait = stopTime - System.currentTimeMillis() ;
          waitingAckMessages.wait(timeToWait);
        }
      }
    }
    close();
  }
  
  public void close() {
    if(waitingAckMessages.size() > 0) {
      System.err.println("There are " + waitingAckMessages.size() + " messages waitting for ack") ;
    }
    client.close(); 
  }
  
  class MessageResponseHandler implements ResponseHandler {
    public void onResponse(HttpResponse response) {
      if(response instanceof HttpContent) {
        HttpContent content = (HttpContent) response;
        String json = content.content().toString(CharsetUtil.UTF_8);
        Ack ack = JSONSerializer.INSTANCE.fromString(json, Ack.class) ;
        String messageId = (String) ack.getMessageId() ;
        Message message = waitingAckMessages.get(messageId) ;
        if(!Ack.Status.OK.equals(ack.getStatus())) {
          onFailedMessage(ack, message) ;
        }
        synchronized(waitingAckMessages) {
          waitingAckMessages.remove(messageId) ;
          waitingAckMessages.notify() ;
        }
      }
    }
  }
}

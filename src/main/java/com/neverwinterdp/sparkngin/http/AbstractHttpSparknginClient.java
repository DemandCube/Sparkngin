package com.neverwinterdp.sparkngin.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringEncoder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.sparkngin.Ack;

abstract public class AbstractHttpSparknginClient {
  private String path = "/message/json" ;
  private AsyncHttpClient client ;
  private LinkedHashMap<String, Message> waitingAckMessages ;
  private int bufferSize ;
  private int sendCount = 0;
  private int errorCount = 0;
  
  public AbstractHttpSparknginClient(String host, int port, int bufferSize, boolean connect) throws Exception {
    client = new AsyncHttpClient(host, port, new MessageResponseHandler(), connect) ;
    this.bufferSize = bufferSize ;
    waitingAckMessages = new LinkedHashMap<String, Message>() ;
  }
  
  public boolean isConnected() { return client.isConnected() ; }
  
  public void setNotConnected() { client.setNotConnected(); }
  
  public boolean connect() throws Exception {
    return client.connect();
  }
  
  public boolean connect(long timeout, long tryPeriod) throws Exception {
    return client.connect(timeout, tryPeriod);
  }
  
  public void setPath(String path) { this.path = path ; }
  
  public int getSendCount() { return this.sendCount ; }
  
  public int getErrorCount() { return errorCount ;}
  
  public void sendGet(Message message, long timeout) throws Exception {
    synchronized(waitingAckMessages) {
      if(waitingAckMessages.size() >= bufferSize) {
        waitingAckMessages.wait(timeout);
        if(waitingAckMessages.size() >= bufferSize) {
          throw new TimeoutException("fail to send the message in " + timeout + "ms") ;
        }
      }
      QueryStringEncoder encoder = new QueryStringEncoder(path);
      encoder.addParam("data", toStringData(message));
      client.get(encoder.toString());
      sendCount++ ;
      String messageId = message.getHeader().getKey() ;
      waitingAckMessages.put(messageId, message) ;
    }
  }
  
  public void sendPost(Message message, long timeout) throws Exception {
    synchronized(waitingAckMessages) {
      if(waitingAckMessages.size() >= bufferSize) {
        waitingAckMessages.wait(timeout);
        if(waitingAckMessages.size() >= bufferSize) {
          throw new TimeoutException("fail to send the message in " + timeout + "ms") ;
        }
      }
      client.post(path, toBinData(message));
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
  
  abstract protected byte[] toBinData(Message message)  ;
  abstract protected String toStringData(Message message)  ;
  abstract protected Ack    toAck(HttpContent content)  ;
  
  protected class MessageResponseHandler implements ResponseHandler {
    public void onResponse(HttpResponse response) {
      if(response instanceof HttpContent) {
        Ack ack = toAck((HttpContent) response) ;
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

package com.neverwinterdp.sparkngin.http;

import java.util.concurrent.LinkedBlockingQueue;

import com.neverwinterdp.message.Message;

public class MessageForwarderQueue {
  private MessageForwarder forwarder ;
  private LinkedBlockingQueue<Message> queue ;
  private Thread forwarderThread ;
  private int wakeupCount = 0;
  
  public MessageForwarderQueue(MessageForwarder mforwarder, int bufferSize) {
    this.forwarder = mforwarder ;
    queue = new LinkedBlockingQueue<Message>(bufferSize) ;
    forwarderThread = new Thread("") {
      public void run() {
        try {
          while(true) {
            //TODO: need to fix this queue, it is not reliable. Also there is a problem if the 
            //queue is full , the queue will block the client thread.
            Message message = queue.take() ;
            forwarder.forward(message);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    forwarderThread.start() ;
  }
  
  int put = 0 ;
  
  public void put(Message message) throws InterruptedException {
    queue.put(message) ;
  }
}

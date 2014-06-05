package com.neverwinterdp.sparkngin.http;

import java.util.ArrayList;
import java.util.List;
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
            List<Message> holder = new ArrayList<Message>() ;
            holder.add(queue.take()) ; //block to make sure some data is available
            queue.drainTo(holder, 500) ;
            for(int i = 0; i < holder.size(); i++) {
              forwarder.forward(holder.get(i));
            }
          }
        } catch(InterruptedException ex) {
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          forwarder.onDestroy();
        }
      }
    };
    forwarderThread.start() ;
  }
  
  int put = 0 ;
  
  public void put(Message message) throws InterruptedException {
    queue.put(message) ;
  }
  
  public void close() {
    forwarderThread.interrupt() ; 
  }
}

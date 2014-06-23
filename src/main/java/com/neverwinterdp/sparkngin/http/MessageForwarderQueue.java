package com.neverwinterdp.sparkngin.http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.codahale.metrics.Timer;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;

public class MessageForwarderQueue {
  private MessageForwarder forwarder ;
  private LinkedBlockingQueue<Message> queue ;
  private Thread forwarderThread ;
  private ComponentMonitor monitor ;
  private int wakeupCount = 0;
  
  public MessageForwarderQueue(ApplicationMonitor appMonitor, MessageForwarder mforwarder, int bufferSize) {
    this.monitor = appMonitor.createComponentMonitor(getClass()) ;
    this.forwarder = mforwarder ;
    queue = new LinkedBlockingQueue<Message>(bufferSize) ;
    forwarderThread = new Thread("message-forwarder") {
      public void run() {
        try {
          while(true) {
            //TODO: need to fix this queue, it is not reliable. Also there is a problem if the 
            //queue is full , the queue will block the client thread.
            List<Message> holder = new ArrayList<Message>() ;
            Timer.Context ctx = monitor.timer("take(Message)").time() ;
            holder.add(queue.take()) ; //block to make sure some data is available
            queue.drainTo(holder, 500) ;
            ctx.close();
            
            for(int i = 0; i < holder.size(); i++) {
              ctx = monitor.timer("forward(Message)").time() ;
              forwarder.forward(holder.get(i));
              ctx.close() ; 
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
    Timer.Context ctx = monitor.timer("put(Message)").time() ;
    queue.put(message) ;
    ctx.stop() ;
  }
  
  public void close() {
    forwarderThread.interrupt() ; 
  }
}

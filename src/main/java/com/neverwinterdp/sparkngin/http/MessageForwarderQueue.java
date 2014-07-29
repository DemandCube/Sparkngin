package com.neverwinterdp.sparkngin.http;

import com.codahale.metrics.Timer;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.queue.Queue;
import com.neverwinterdp.sparkngin.queue.Segment;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;

public class MessageForwarderQueue {
  private MessageForwarder forwarder ;
  private Queue<Message> queue ;
  private Thread forwarderThread ;
  private ComponentMonitor monitor ;
  
  public MessageForwarderQueue(ApplicationMonitor appMonitor, MessageForwarder mforwarder, String storeDir) throws Exception {
    this.monitor = appMonitor.createComponentMonitor(getClass()) ;
    this.forwarder = mforwarder ;
    queue = new Queue<Message>(storeDir, 10000l) ;
    forwarderThread = new Thread("message-forwarder") {
      public void run() {
        try {
          while(true) {
            Segment<Message> segment = queue.nextReadSegment(1000l) ;
            if(segment != null) {
              segment.open(); 
              while(segment.hasNext()) {
                Timer.Context readCtx = monitor.timer("read(Message)").time() ;
                Message message = segment.next() ;
                readCtx.close(); 
                
                Timer.Context forwardCtx = monitor.timer("forward(Message)").time() ;
                forwarder.forward(message);
                forwardCtx.close();
              }
              queue.commitReadSegment(segment);
            }
          }
        } catch(InterruptedException ex) {
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          forwarder.close();
        }
      }
    };
    forwarderThread.start() ;
  }
  
  int put = 0 ;
  
  public void put(Message message) throws Exception {
    Timer.Context ctx = monitor.timer("put(Message)").time() ;
    queue.write(message);
    ctx.stop() ;
  }
  
  public void close() {
    forwarderThread.interrupt() ; 
  }
}

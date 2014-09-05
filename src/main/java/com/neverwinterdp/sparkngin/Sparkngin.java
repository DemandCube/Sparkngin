package com.neverwinterdp.sparkngin;

import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.sparkngin.queue.Queue;
import com.neverwinterdp.sparkngin.queue.Segment;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;

public class Sparkngin {
  @Inject @Named("sparkngin:forwarder-class")
  private String forwarderClass = NullDevMessageForwarder.class.getName() ;
  
  @Inject(optional = true) @Named("sparkngin:queue-dir")
  private String queueDir ;
  
  private MessageForwarder forwarder ;
  private Queue<Message> queue ;
  private Thread forwarderThread ;
  private ComponentMonitor monitor ;
  
  public Sparkngin()  {}
  
  public Sparkngin(ApplicationMonitor appMonitor, MessageForwarder mforwarder, String storeDir) throws Exception {
    this.monitor = appMonitor.createComponentMonitor(getClass()) ;
    this.forwarder = mforwarder ;
    queue = new Queue<Message>(storeDir, 10000l) ;
    forwarderThread = new ForwarderThread() ;
    forwarderThread.start() ;
  }
  
  @Inject
  public void init(Injector container, ModuleProperties moduleProperties, ApplicationMonitor appMonitor) throws Exception {
    this.monitor = appMonitor.createComponentMonitor(getClass()) ;
    Class<MessageForwarder> type = (Class<MessageForwarder>) Class.forName(forwarderClass) ;
    forwarder = container.getInstance(type) ;
    if(queueDir == null) {
      queueDir = moduleProperties.getDataDir() + "/sparkngin/queue" ;
    }
    queue = new Queue<Message>(queueDir, 10000l) ;
    forwarderThread = new ForwarderThread() ;
    forwarderThread.start() ;
  }
  
  public String getQueueDir() { return this.queueDir ; }
  
  public boolean cleanup() throws Exception {
    queue.close(); 
    FileUtil.removeIfExist(queueDir, false);
    queue = new Queue<Message>(queueDir, 10000l) ;
    return true ;
  }
  
  /**
   * Enqueue the message and the forwarder thread will forward the message later.
   * @param message
   * @throws Exception
   */
  public Ack push(Message message) {
    Timer.Context ctx = monitor.timer("push").time() ;
    Ack ack = new Ack() ;
    try {
      queue.write(message);
      ack.setMessageId(message.getHeader().getKey());
      ack.setStatus(Ack.Status.OK);
    } catch (Exception e) {
      ack.setMessage(e.getMessage());
      ack.setStatus(Ack.Status.ERROR);
    }
    ctx.stop() ;
    return ack ;
  }
  
  /**
   * Forward the message immediatelly
   * @param message
   * @throws Exception
   */
  public void forward(Message message) throws Exception {
    Timer.Context forwardCtx = monitor.timer("forward").time() ;
    forwarder.forward(message);
    forwardCtx.close();
  }
  
  public void close() {
    forwarderThread.interrupt() ; 
    forwarder.close();
  }
  
  public class ForwarderThread extends Thread {
    public ForwarderThread() {
      super("SparknginForwarderThread");
      setPriority(Thread.MAX_PRIORITY);
    }
    
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
              forward(message) ;
            }
            queue.commitReadSegment(segment);
          }
        }
      } catch(InterruptedException ex) {
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
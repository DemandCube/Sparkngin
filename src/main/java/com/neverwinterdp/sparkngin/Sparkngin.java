package com.neverwinterdp.sparkngin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.neverwinterdp.buffer.chronicle.MultiSegmentQueue;
import com.neverwinterdp.buffer.chronicle.Segment;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;

public class Sparkngin {
  @Inject @Named("sparkngin:forwarder-class")
  private String forwarderClass = NullDevMessageForwarder.class.getName() ;
  
  @Inject(optional = true) @Named("sparkngin:forwarder-reconnect")
  private long forwardReconnect = 10000 ;
  
  
  @Inject(optional = true) @Named("sparkngin:queue-dir")
  private String queueDir ;
  
  private MultiSegmentQueue<Message> queue ;
  private ForwarderThread forwarderThread ;
  private MetricRegistry mRegistry ;
  
  public Sparkngin()  {}
  
  public Sparkngin(MetricRegistry mRegistry, MessageForwarder mforwarder, String storeDir) throws Exception {
    this.mRegistry = mRegistry ;
    queue = new MultiSegmentQueue<Message>(storeDir, 10000l) ;
    forwarderThread = new ForwarderThread(mforwarder) ;
    forwarderThread.start() ;
  }
  
  @Inject
  public void init(Injector container, ModuleProperties moduleProperties, MetricRegistry mRegistry) throws Exception {
    this.mRegistry = mRegistry ;
    Class<MessageForwarder> type = (Class<MessageForwarder>) Class.forName(forwarderClass) ;
    if(queueDir == null) {
      queueDir = moduleProperties.getDataDir() + "/sparkngin/queue" ;
    }
    queue = new MultiSegmentQueue<Message>(queueDir, 10000l) ;
    MessageForwarder forwarder = container.getInstance(type) ;
    forwarderThread = new ForwarderThread(forwarder) ;
    forwarderThread.start() ;
  }
  
  public String getQueueDir() { return this.queueDir ; }
  
  public boolean cleanup() throws Exception {
    queue.close(); 
    FileUtil.removeIfExist(queueDir, false);
    queue = new MultiSegmentQueue<Message>(queueDir, 10000l) ;
    return true ;
  }
  
  /**
   * Enqueue the message and the forwarder thread will forward the message later.
   * @param message
   * @throws Exception
   */
  public Ack push(Message message) {
    Timer.Context ctx = mRegistry.timer(Sparkngin.class.getSimpleName(), "push").time() ;
    Ack ack = new Ack() ;
    try {
      queue.writeObject(message);
      ack.setMessageId(message.getHeader().getKey());
      ack.setStatus(Ack.Status.OK);
    } catch (Exception e) {
      ack.setMessage(e.getMessage());
      ack.setStatus(Ack.Status.ERROR);
    }
    ctx.stop() ;
    return ack ;
  }
  
  public void close() {
    forwarderThread.exit = true;
    forwarderThread.interrupt() ; 
  }
  
  public class ForwarderThread extends Thread {
    private MessageForwarder forwarder ;
    private boolean exit = false;
    
    public ForwarderThread(MessageForwarder forwarder) {
      super("SparknginForwarderThread");
      setPriority(Thread.MAX_PRIORITY);
      this.forwarder = forwarder ;
    }
    
    /**
     * Forward the message immediatelly
     * @param message
     * @throws Exception
     */
    void forward(Message message) throws Exception {
      Timer.Context forwardCtx = mRegistry.timer(Sparkngin.class.getSimpleName(), "forward").time() ;
      forwarder.forward(message);
      forwardCtx.close();
    }
    
    public void forward() throws InterruptedException, Exception {
      while(true) {
        Segment<Message> segment = queue.nextReadSegment(1000l) ;
        if(segment != null) {
          segment.open(); 
          Throwable forwardError = null;
          while(forwardError == null && segment.hasNext()) {
            Timer.Context readCtx = mRegistry.timer(Sparkngin.class.getSimpleName(), "read").time() ;
            Message message = segment.nextObject() ;
            readCtx.close();
            try {
              forward(message) ;
            } catch(Exception ex) {
              forwardError = ex ;
            }
          }
          if(forwardError == null) {
            queue.commitReadSegment(segment);
          } else {
            segment.close();
            forwarder.setError(forwardError);
            System.err.println("sparkngin forward error: " + forwardError.getMessage() + ". Retry in " + forwardReconnect + "ms");
            Thread.sleep(forwardReconnect);
            forwarder.reconnect();
          }
        }
      }
    }
    
    public void run() {
      try {
        forward() ;
      } catch (InterruptedException e) {
      } catch (Exception e) {
        e.printStackTrace();
      }
      forwarder.close();
    }
  }
}
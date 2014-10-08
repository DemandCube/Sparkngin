package com.neverwinterdp.sparkngin.log4j;

import java.io.IOException;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.neverwinterdp.buffer.chronicle.MultiSegmentQueue;
import com.neverwinterdp.buffer.chronicle.Segment;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.http.JSONHttpSparknginClient;

public class SparknginLog4jAppender extends AppenderSkeleton {
  private String   queueBufferDir;
  private int      queueMaxSizePerSegment = 100000;
  private String   sparknginHost ;
  private int      sparknginPort ;
  private long     sparknginReconnectPeriod = 10000;
  private String   messageTopic ;
  private boolean  queueError = false ;
  private MultiSegmentQueue<Log4jRecord> queue ; 
  
  private DeamonThread forwardThread ;
  
  public void close() {
    if(forwardThread != null) {
      forwardThread.exit = true ;
      forwardThread.interrupt() ; 
    }
  }

  public void activateOptions() {
    System.out.println("SparknginLog4jAppender: Start Activate Elasticsearch log4j appender");
    try {
      queue = new MultiSegmentQueue<Log4jRecord>(queueBufferDir, queueMaxSizePerSegment) ;
    } catch (Exception e) {
      queueError = true ;
      e.printStackTrace();
    }
    forwardThread = new DeamonThread(); 
    forwardThread.setDaemon(true);
    forwardThread.start() ;
    System.out.println("SparknginLog4jAppender: Finish Activate Elasticsearch log4j appender");
  }

  public void setQueueBufferDir(String queueBufferDir) { this.queueBufferDir = queueBufferDir; }

  public void setQueueMaxSizePerSegment(int queueMaxSizePerSegment) {
    this.queueMaxSizePerSegment = queueMaxSizePerSegment;
  }

  public void setSparknginHost(String sparknginHost) {
    this.sparknginHost = sparknginHost;
  }

  public void setSparknginPort(int sparknginPort) {
    this.sparknginPort = sparknginPort;
  }

  public void setSparknginReconnectPeriod(long sparknginReconnectPeriod) {
    this.sparknginReconnectPeriod = sparknginReconnectPeriod;
  }

  public void setMessageTopic(String messageTopic) {
    this.messageTopic = messageTopic;
  }

  public boolean requiresLayout() { return false; }

  protected void append(LoggingEvent event) {
    if(queueError) return ;
    Log4jRecord record = new Log4jRecord(event) ;
    try {
      queue.writeObject(record) ;
    } catch (Exception e) {
      queueError = true ;
      e.printStackTrace();
    }
  }
  
  public class DeamonThread extends Thread {
    private JSONHttpSparknginClient client = null ;
    private boolean exit = false; 
    
    boolean init() {
      try {
        client = new JSONHttpSparknginClient(sparknginHost, sparknginPort, 300, false) ;
        return client.connect(60 * 60 * 1000, sparknginReconnectPeriod) ;
      } catch(Exception ex) {
        return false ;
      }
    }
    
    public void forward() {
      while(true) {
        try {
          if(!client.isConnected()) {
            if(!client.connect(60 * 60 * 1000, sparknginReconnectPeriod)) continue ;
          }
          Segment<Log4jRecord> segment = null ;
          while((segment = queue.nextReadSegment(15000)) != null) {
            segment.open();
            while(segment.hasNext()) {
              Log4jRecord record = segment.nextObject() ;
              Message message = new Message(record.getId(), record, false) ;
              message.getHeader().setTopic(messageTopic);
              client.sendPost(message, 30000);
            }
            queue.commitReadSegment(segment);
          }
        } catch (InterruptedException e) {
          return ;
        } catch(Exception ex) {
          client.close(); 
          client.setNotConnected();
        }
      }
    }
    
    void shutdown() { 
      client.close() ;
      if(exit) {
        try {
          if(queue != null) queue.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    public void run() {
      if(!init()) return ;
      forward() ;
      shutdown() ;
    }
  }
}
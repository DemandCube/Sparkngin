package com.neverwinterdp.sparkngin.log4j;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.spi.LoggingEvent;

public class Log4jRecord implements Serializable {
  static AtomicLong idTracker = new AtomicLong() ;
  
  private long   timestamp;
  private String threadName ;
  private String loggerName;
  private String level;
  private String message;

  public Log4jRecord() {
  }

  public Log4jRecord(LoggingEvent event) {
    this.timestamp = event.getTimeStamp();
    this.threadName = event.getThreadName() ;
    this.loggerName = event.getLoggerName();
    this.level = event.getLevel().toString();
    this.message = event.getRenderedMessage();
  }

  public String getId() {
    return "id-"  + this.timestamp + "-" + idTracker.incrementAndGet() ;
  }
  
  public long getTimestamp() { return timestamp; }

  public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

  public String getThreadName() { return threadName; }

  public void setThreadName(String threadName) { this.threadName = threadName; }

  public String getLoggerName() { return loggerName; }

  public void setLoggerName(String loggerName) { this.loggerName = loggerName; }

  public String getLevel() { return level; }

  public void setLevel(String level) { this.level = level; }

  public String getMessage() { return message; }

  public void setMessage(String message) { this.message = message; }
}

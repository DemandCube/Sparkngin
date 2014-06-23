package com.neverwinterdp.sparkngin.queue;

public class QueueConfig {
  private long maxSizePerSegment = 1024 * 1024; //Default is 1M

  public long getMaxSizePerSegment() { return maxSizePerSegment; }
  public void setMaxSizePerSegment(long maxSizePerSegment) { this.maxSizePerSegment = maxSizePerSegment; }
}

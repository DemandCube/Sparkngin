package com.neverwinterdp.sparkngin.queue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;

import com.neverwinterdp.util.FileUtil;

public class Queue<T extends Serializable> {
  private String              storeDir;
  private LinkedList<Segment<T>> segments;
  private Segment<T>             writting ;
  private long maxSizePerSegment ;
  private int segmentIndexTracker = 0 ;
  
  public Queue(String storeDir, long maxSizePerSegment) throws Exception {
    this.storeDir = storeDir ;
    this.maxSizePerSegment = maxSizePerSegment ;
    segments = new LinkedList<Segment<T>>() ;
    if(!FileUtil.exist(storeDir)) {
      FileUtil.mkdirs(storeDir) ;
    } else {
      File dir = new File(storeDir) ;
      File[] fsegments = dir.listFiles(new SegmentFileFilter()) ;
      for(File selSegment : fsegments) {
        String fileName = selSegment.getName() ;
        String numString = fileName.substring("segment-".length(), fileName.lastIndexOf('.')) ;
        int segIndex = Integer.parseInt(numString) ;
        Segment<T> segment = new Segment<T>(storeDir, segIndex, maxSizePerSegment) ;
        segments.add(segment) ;
        if(segmentIndexTracker < segIndex) segmentIndexTracker = segIndex + 1 ;
      }
    }
  }
  
  synchronized public void write(T object) throws Exception {
    if(writting != null && writting.isFull()) {
      newWritingSegment() ;
    }
    if(writting == null) {
      writting = new Segment<T>(storeDir, segmentIndexTracker++, maxSizePerSegment) ;
      writting.open() ;
    }
    writting.append(object) ;
  }
  
  public Segment<T> nextReadSegment(long wait) throws InterruptedException, IOException {
    if(segments.size() > 0) return segments.getFirst() ;
    synchronized(segments) {
      segments.wait(wait) ;
      newWritingSegment() ;
      if(segments.size() > 0) return segments.getFirst() ;
    }
    return null ;
  }
  
  public void commitReadSegment(Segment<T> segment) throws Exception {
    synchronized(segments) {
      segments.remove(segment) ;
      segment.delete();
    }
  }
  
  synchronized void newWritingSegment() throws IOException {
    if(writting == null) return ;
    writting.close() ;
    synchronized(segments) {
      segments.addLast(writting);
      segments.notifyAll();
    }
    writting = null ;
  }
  
  public void close() throws IOException {
    if(writting != null) {
      writting.close() ;
      writting = null ;
    }
  }
  
  static public class SegmentFileFilter implements FileFilter {
    public boolean accept(File pathname) {
      String fname = pathname.getName() ;
      if(fname.startsWith("segment") && fname.endsWith(".index")) return true ;
      return false;
    }
  }
}

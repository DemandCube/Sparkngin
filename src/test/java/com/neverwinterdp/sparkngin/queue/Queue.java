package com.neverwinterdp.sparkngin.queue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;

import com.neverwinterdp.util.FileUtil;

public class Queue<T extends Serializable> {
  private String              storeDir;
  private QueueConfig         config = new QueueConfig();
  private LinkedList<Segment<T>> segments;
  private Segment<T>             writting ;
  
  public Queue(String storeDir) throws Exception {
    this.storeDir = storeDir ;
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
        Segment<T> segment = new Segment<T>(storeDir, segIndex) ;
        segments.add(segment) ;
      }
    }
  }
  
  synchronized public void write(T object) throws Exception {
    if(writting != null && writting.isFull()) {
      writting.close() ;
      segments.addLast(writting);
      writting = null ;
    }
    if(writting == null) {
      int nextSegmentIndex = 0 ;
      if(segments.size() > 0) {
        Segment<T> lastSegment = segments.getLast() ;
        nextSegmentIndex = lastSegment.getIndex()  + 1;
      }
      writting = new Segment<T>(storeDir, nextSegmentIndex) ;
      writting.open() ;
    }
    writting.append(object) ;
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

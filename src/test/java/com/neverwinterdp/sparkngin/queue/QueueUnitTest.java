package com.neverwinterdp.sparkngin.queue;

import org.junit.Test;

import com.neverwinterdp.util.FileUtil;

public class QueueUnitTest {
  @Test
  public void testQueue() throws Exception {
    testQueue(10000, 5000) ;
    testQueue(1000000, 5000) ;
  }
  
  
  void testQueue(int size, long sizePerSegment) throws Exception {
    FileUtil.removeIfExist("build/queue", false);
    long start = System.currentTimeMillis() ;
    Queue<byte[]> queue = new Queue<byte[]>("build/queue", sizePerSegment) ;
    byte[] data = new byte[1024] ;
    for(int i = 0; i < data.length; i++) {
      data[i] = (byte)((i % 32) + 32) ;
    }
    for(int i = 0; i < size; i++) {
      queue.write(data);
    }
    long stop = System.currentTimeMillis() ;
    System.out.println("Insert " + size + " in " + (stop - start) + "ms");
    
    start = System.currentTimeMillis() ;
    Segment<byte[]> segment = null ;
    int read = 0 ;
    while((segment = queue.nextReadSegment(100)) != null) {
      segment.open() ;
      while(segment.hasNext()) {
        read++ ;
      }
      queue.commitReadSegment(segment);
    }
    stop = System.currentTimeMillis() ;
    System.out.println("Read " + read + " in " + (stop - start) + "ms");
    queue.close();
    
  }
  
}

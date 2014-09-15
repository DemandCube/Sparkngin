package com.neverwinterdp.sparkngin.queue;

import org.junit.Test;

import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.yara.MetricPrinter;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;

public class QueueUnitTest {
  @Test
  public void testQueue() throws Exception {
    testQueue(10000, 10000) ;
    testQueue(300000, 50000) ;
  }
  
  
  void testQueue(int size, long sizePerSegment) throws Exception {
    FileUtil.removeIfExist("build/queue", false);
    MetricRegistry mRegistry = new MetricRegistry() ;
    long start = System.currentTimeMillis() ;
    Queue<byte[]> queue = new Queue<byte[]>("build/queue", sizePerSegment) ;
    byte[] data = new byte[1024] ;
    for(int i = 0; i < data.length; i++) {
      data[i] = (byte)((i % 32) + 32) ;
    }
    for(int i = 0; i < size; i++) {
      Timer.Context timeCtx = mRegistry.timer("queue", "write").time() ;
      queue.write(data);
      timeCtx.close(); 
    }
    long stop = System.currentTimeMillis() ;
    System.out.println("Insert " + size + " in " + (stop - start) + "ms");
    new MetricPrinter().print(mRegistry);
    
    mRegistry.remove("*") ;
    start = System.currentTimeMillis() ;
    Segment<byte[]> segment = null ;
    int read = 0 ;
    start = System.currentTimeMillis() ;
    while((segment = queue.nextReadSegment(100)) != null) {
      segment.open() ;
      while(segment.hasNext()) {
        Timer.Context timeCtx = mRegistry.timer("queue", "next").time() ;
        segment.next() ;
        read++ ;
        timeCtx.close(); 
      }
      queue.commitReadSegment(segment);
    }
    stop = System.currentTimeMillis() ;
    System.out.println("Read " + read + " in " + (stop - start) + "ms");
    queue.close();
    new MetricPrinter().print(mRegistry);
  }
  
}

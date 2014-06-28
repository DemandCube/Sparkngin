package com.neverwinterdp.sparkngin.queue;

import org.junit.Test;

import com.codahale.metrics.Timer;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;
import com.neverwinterdp.util.monitor.snapshot.MetricFormater;

public class QueueUnitTest {
  @Test
  public void testQueue() throws Exception {
    testQueue(10000, 10000) ;
    testQueue(300000, 50000) ;
  }
  
  
  void testQueue(int size, long sizePerSegment) throws Exception {
    FileUtil.removeIfExist("build/queue", false);
    ApplicationMonitor appMonitor = new ApplicationMonitor() ;
    ComponentMonitor monitor = appMonitor.createComponentMonitor(Queue.class) ;
    long start = System.currentTimeMillis() ;
    Queue<byte[]> queue = new Queue<byte[]>("build/queue", sizePerSegment) ;
    byte[] data = new byte[1024] ;
    for(int i = 0; i < data.length; i++) {
      data[i] = (byte)((i % 32) + 32) ;
    }
    for(int i = 0; i < size; i++) {
      Timer.Context timeCtx = monitor.timer("write()").time() ;
      queue.write(data);
      timeCtx.close(); 
    }
    long stop = System.currentTimeMillis() ;
    System.out.println("Insert " + size + " in " + (stop - start) + "ms");
    MetricFormater formater = new MetricFormater() ;
    System.out.println(formater.format(appMonitor.snapshot().getRegistry().getTimers())) ;
    
    appMonitor.remove("*") ;
    start = System.currentTimeMillis() ;
    Segment<byte[]> segment = null ;
    int read = 0 ;
    start = System.currentTimeMillis() ;
    while((segment = queue.nextReadSegment(100)) != null) {
      segment.open() ;
      while(segment.hasNext()) {
        Timer.Context timeCtx = monitor.timer("next()").time() ;
        segment.next() ;
        read++ ;
        timeCtx.close(); 
      }
      queue.commitReadSegment(segment);
    }
    stop = System.currentTimeMillis() ;
    System.out.println("Read " + read + " in " + (stop - start) + "ms");
    queue.close();
    System.out.println(formater.format(appMonitor.snapshot().getRegistry().getTimers())) ;
  }
  
}

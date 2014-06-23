package com.neverwinterdp.sparkngin.queue;

import org.junit.Test;

public class QueueUnitTest {
  @Test
  public void testQueue() throws Exception {
    Queue<String> queue = new Queue<String>("build/queue") ;
    for(int i = 0; i < 100000; i++) {
      queue.write("This is a test , This is a test, This is a test" + i);
    }
    queue.close();
  }
  
}

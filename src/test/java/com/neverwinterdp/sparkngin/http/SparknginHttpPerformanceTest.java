package com.neverwinterdp.sparkngin.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.yara.MetricPrinter;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpPerformanceTest {

  private SparknginServer server;
  
  @Before
  public void setup() throws Exception {
    server = new SparknginServer() ;
  }
  
  @After
  public void teardown() {
    server.shutdown();
  }
  
  @Test
  public void testPerformance() throws Exception {
    runProfile(1, 10000);
    int NUM = 100000;
    runProfile(1, NUM);
    //runProfile(2, NUM);
    //runProfile(4, NUM);
  }
  
  public void runProfile(int numOfThread, int numOfMessagePerThread) throws Exception {
    long start = System.currentTimeMillis();
    server.metricRegistry.remove("*");
    Thread[] thread = new Thread[numOfThread];
    for(int i = 0; i < thread.length; i++) {
      thread[i] = new Thread(new Producer(server.metricRegistry, numOfMessagePerThread));
      thread[i].start(); 
    }
    boolean finished = false;
    while(!finished) {
      Thread.sleep(1000);
      System.out.print(".");
      finished = true;
      for(int i = 0; i < thread.length; i++) {
        if(thread[i].isAlive()) {
          finished = false;
          break;
        }
      }
    }
    long elapsed = System.currentTimeMillis() - start;
    System.out.println("\nRun test in  " + elapsed + "ms");
    new MetricPrinter().print(server.metricRegistry) ;
  }
  
  static public class Producer implements Runnable {
    int NUM_OF_MESSAGES;
    MetricRegistry clientMonitor;
    
    public Producer(MetricRegistry appMonitor, int num) {
      NUM_OF_MESSAGES = num;
      clientMonitor = appMonitor;
    }
    
    public void run() {
      try {
        JSONHttpSparknginClient mclient = new JSONHttpSparknginClient("127.0.0.1", 7080, 500, true);
        int hashCode = hashCode();
        for(int i = 0; i < NUM_OF_MESSAGES; i++) {
          Message message = new Message("m-" + hashCode + "-"+ i, new byte[1024], true);
          Timer.Context ctx = clientMonitor.timer("sparkngin", "client", "send").time();
          mclient.sendPost(message, 5000);
          ctx.stop();
        }
        mclient.waitAndClose(30000);
      } catch(Exception ex) {
        ex.printStackTrace(); 
      }
    }
  }
}
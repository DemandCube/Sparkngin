package com.neverwinterdp.sparkngin.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Timer;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.util.FileUtil;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;
import com.neverwinterdp.util.monitor.snapshot.MetricFormater;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginHttpPerformanceTest {
  static {
    System.setProperty("log4j.configuration", "file:src/main/resources/log4j.properties");
  }

  private NullDevMessageForwarder forwarder;
  private HttpServer server;
  private ApplicationMonitor appMonitor ;
  
  @Before
  public void setup() throws Exception {
    FileUtil.removeIfExist("build/queue", false);
    forwarder = new NullDevMessageForwarder();
    server = new HttpServer();
    server.setPort(7080);
    appMonitor = new ApplicationMonitor();
    server.add("/message", new MessageRouteHandler(appMonitor, forwarder, "build/queue/data"));
    server.startAsDeamon();
    Thread.sleep(2000);
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
    appMonitor.remove("*");
    Thread[] thread = new Thread[numOfThread];
    for(int i = 0; i < thread.length; i++) {
      thread[i] = new Thread(new Producer(appMonitor, numOfMessagePerThread));
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
    MetricFormater formater = new MetricFormater();
    System.out.println(formater.format(appMonitor.snapshot().getRegistry().getTimers()));
  }
  
  static public class Producer implements Runnable {
    int NUM_OF_MESSAGES;
    ComponentMonitor clientMonitor;
    
    public Producer(ApplicationMonitor appMonitor, int num) {
      NUM_OF_MESSAGES = num;
      clientMonitor = appMonitor.createComponentMonitor(AsyncHttpClient.class);
    }
    
    public void run() {
      try {
        HttpMessageClient mclient = new HttpMessageClient("127.0.0.1", 7080, 500);
        int hashCode = hashCode();
        for(int i = 0; i < NUM_OF_MESSAGES; i++) {
          Message message = new Message("m-" + hashCode + "-"+ i, new byte[1024], true);
          Timer.Context ctx = clientMonitor.timer("post()").time();
          mclient.send(message, 5000);
          ctx.stop();
        }
        mclient.waitAndClose(30000);
      } catch(Exception ex) {
        ex.printStackTrace(); 
      }
    }
  }
}
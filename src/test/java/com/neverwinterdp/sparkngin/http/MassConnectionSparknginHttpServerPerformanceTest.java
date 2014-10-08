package com.neverwinterdp.sparkngin.http;

import static org.junit.Assert.assertEquals;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.ResponseHandler;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MassConnectionSparknginHttpServerPerformanceTest {
  int NUM_OF_SERVER_THREAD = 5 ;
  int NUM_OF_CONCURRENT_WORKERS = 5 * NUM_OF_SERVER_THREAD ;
  int NUM_OF_WORKERS = 2 * NUM_OF_CONCURRENT_WORKERS ;
  int NUM_OF_MESSAGE_PER_WORKER = 1000 ;
  int EXPECT_MESSAGES_SENT = NUM_OF_WORKERS * NUM_OF_MESSAGE_PER_WORKER ; 
  
  private SparknginServer server ;
  private AtomicLong messageCounter = new AtomicLong() ;

  @Before
  public void setup() throws Exception {
    server = new SparknginServer() ;
  }

  @After
  public void teardown() {
    server.shutdown() ;
  }

  @Test
  public void testMassConnection() throws Exception {
    //Thread.sleep(30000);
    //ExecutorService executor = Executors.newScheduledThreadPool(NUM_OF_CONCURRENT_WORKERS);
    ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_CONCURRENT_WORKERS);
    long startTime = System.currentTimeMillis() ;
    for(int i = 0; i < NUM_OF_WORKERS; i++) {
      executor.submit(new MessageProducer(NUM_OF_MESSAGE_PER_WORKER));
    }
    executor.shutdown() ;
    executor.awaitTermination(10, TimeUnit.MINUTES) ;
    long executeTime = System.currentTimeMillis() - startTime ;
    System.out.println("Expect sent:  " + EXPECT_MESSAGES_SENT) ;
    System.out.println("Sent:         " + messageCounter.get());
    System.out.println("Forward:      " + server.forwarder.getProcessCount());
    System.out.println("Execute Time: " + executeTime + "ms");
    System.out.println("Forward After 30s:      " + server.forwarder.getProcessCount());
  }

  public class MessageProducer implements Runnable {
    int numOfMessages ;

    public MessageProducer(int numOfMessages) {
      this.numOfMessages = numOfMessages ;
    }

    public void run() {
      JSONHttpSparknginClient client = null ;
      try {
        client = new JSONHttpSparknginClient ("127.0.0.1", server.server.getPort(), 100, true) ;
        
        byte[] data = new byte[1024] ;
        for(int i = 0; i < data.length; i++) {
          data[i] = (byte) (i % 255);
        }
        
        for(int i = 0; i < numOfMessages; i++) {
          Message message = new Message("m" + i, data, true) ;
          //The message has to be sent in 5s
          client.sendPost(message, 30000);
          messageCounter.incrementAndGet() ;
        }
        
        client.waitAndClose(5000);
        
        assertEquals(0, client.getErrorCount()) ;
        assertEquals(numOfMessages, client.getSendCount()) ;
      } catch(Throwable ex) {
        System.err.println("Message Producer Error!");
        ex.printStackTrace(); 
        if(client != null) client.close();
      }
    }
  }

  static public class MessageResponseHandler implements ResponseHandler {
    int count = 0; 
    int failure = 0;
    String expectMessage ;

    public MessageResponseHandler(String expectMessage) {
      this.expectMessage = expectMessage ;
    }

    public void onResponse(HttpResponse response) {
      count++;
      HttpContent content = (HttpContent) response;
      String message = content.content().toString(CharsetUtil.UTF_8);
      try{
        assertEquals(expectMessage, message);
      } catch(AssertionError e){
        failure++;
      }
    }

    public int getCount(){ return this.count; }

    public int getFailure(){ return this.failure; }
  }
}
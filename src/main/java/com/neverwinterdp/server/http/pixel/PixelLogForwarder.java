package com.neverwinterdp.server.http.pixel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.util.JSONSerializer;

/**
 * Class used to connect to Sparkngin and forward HTTP messages formatted in RequestLog object format
 * Example usage:
 * <pre>
 * {@code
 * SparknginMessageForwarder forwarder = new SparknginMessageForwarder("127.0.0.1",7080);
 * forwarder.forwardToSpark(new RequestLog(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "test")));
 * }
 * </pre>
 * @author Richard Duarte
 */
public class PixelLogForwarder {
  private AsyncHttpClient sparknginClient = null;
  //Need to use the blocking queue here since we have the netty handler thread that try to add the log to the buffer 
  //and the forwarder thread that dequeue and send the log to sparkngin
  //Note that , this model work only if there is multiple producers and single consumer. The consumer is forwarder thread.
  private BlockingQueue<RequestLog> undeliveredReqLogs ;
  private ForwarderThread forwarderThread ;
  
  private String host;
  private String path;
  private int port;
  //TODO: need to implement your own handler later
  private DumpResponseHandler handler = new DumpResponseHandler();
  private AtomicLong idTracker = new AtomicLong() ;
  
  /**
   * Constructor.  Attempts to connect to sparkngin (non-blocking).  Sets path to "/message"
   * @param Host Hostname of Sparkngin
   * @param Port Port Sparkngin is listening on
   */
  public PixelLogForwarder(String Host, int Port) {
    this(Host,Port,"/message",10000);
  }
  
  /**
   * Constructor.  Attempts to connect to sparkngin (non-blocking)
   * @param host Hostname of Sparkngin
   * @param port Port Sparkngin is listening on
   * @param path Path to send message to, i.e. from http://127.0.0.1:7071/message, then Path would be "/message"
   * @param queueLimit Size of queue of messages to buffer
   */
  public PixelLogForwarder(String host, int port, String path, int queueLimit) {
    this.host = host;
    this.port = port;
    this.path = path;

    undeliveredReqLogs = new LinkedBlockingQueue<RequestLog>(queueLimit) ;
    //Init and run the forwarder thread 
    forwarderThread = new ForwarderThread() ;
    forwarderThread.start() ;
  }
  
  /**
   * Connect to sparkngin and send httpRequest as well as any others that haven't been delivered yet
   * If sparkngin cannot be connected to, then add log to undeliveredReqLogs
   * If undeliveredReqLogs is full, the message will be dropped
   * @param log  The RequestLog to forward
   */
  public boolean forward(RequestLog reqLog){
    //If the buffer is full it won't be added, throw away the log to avoid the buffer overflow. 
    //This happen when the sparkngin service is not available.
    return undeliveredReqLogs.offer(reqLog) ;
  }
  
  AsyncHttpClient getSparknginClient() {
    if(sparknginClient != null) return sparknginClient ;
    //Try for 1 mins
    for(int i = 0; i < 10; i++) {
      try {
        sparknginClient = new AsyncHttpClient(host, port, handler) ;
        return sparknginClient ;
      } catch (Exception e) {
        System.out.println("Cannot get the spakngin connection, " + e.getMessage());
      }
      try {
        Thread.sleep(6000);
      } catch (InterruptedException e) {
      }
    }
    return null ;
  }
  
  void onSparknginClientError(Exception ex) {
    //On sparkngin client error, close the current client connection. Create new client connection on next request.
    if(sparknginClient != null) {
      sparknginClient.close(); 
      sparknginClient = null ;
    }
  }
  
  /**
   * Disconnects AsynchHttpClient from sparkngin
   */
  protected void disconnect(){
    forwarderThread.interrupt();
    if(sparknginClient != null) {
      sparknginClient.close();
    }
  }

  /**
   * @author Tuan
   * The forwarder thread will wait and listen to the log buffer, if there is a log available in the buffer,
   * the forwarder will take the log and send to sparkngin, if it send successfully , the log entry will be removed from
   * the buffer
   */
  public class ForwarderThread extends Thread {
    public void run() {
      //only take the entry in the buffer, do not remove.
      try {
        while(true){
          RequestLog log = null ;
          //Sleep while the buffer is empty
          while(undeliveredReqLogs.peek() == null){
            Thread.sleep(1000);
          }
          while((log = undeliveredReqLogs.peek()) != null) {
            AsyncHttpClient client = getSparknginClient() ;
            if(client != null) {
              String data = JSONSerializer.INSTANCE.toString(log) ;
              //Send request log to sparkngin
              Message message = new Message("id-" + idTracker.incrementAndGet(), data, false) ;
              message.getHeader().setTopic("log.pixel") ;
              try{
                client.post(path, message);
                undeliveredReqLogs.remove();
              } catch (Exception e) {
                System.out.println("Cannot send the log to sparkngin, " + e.getMessage()) ;
                onSparknginClientError(e) ;
                //forward(log) ;
              }
            }
            else{
              //forward(log);
            }
          }
        }
      } catch (InterruptedException e) {}
    }
  }
}

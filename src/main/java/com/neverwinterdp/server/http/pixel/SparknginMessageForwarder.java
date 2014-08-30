package com.neverwinterdp.server.http.pixel;

import java.util.LinkedList;
import java.util.Queue;
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
public class SparknginMessageForwarder {
  private AsyncHttpClient sparknginClient = null;
  private Queue<RequestLog> undeliveredReqLog = new LinkedList<RequestLog>();
  private String host;
  private String path;
  private int port;
  private DumpResponseHandler handler = new DumpResponseHandler();;
  private AtomicLong idTracker = new AtomicLong() ;
  private boolean connected = false;
  
  /**
   * Constructor.  Attempts to connect to sparkngin (non-blocking).  Sets path to "/message"
   * @param Host Hostname of Sparkngin
   * @param Port Port Sparkngin is listening on
   */
  public SparknginMessageForwarder(String Host, int Port){
    this(Host,Port,"/message");
  }
  
  /**
   * Constructor.  Attempts to connect to sparkngin (non-blocking)
   * @param Host Hostname of Sparkngin
   * @param Port Port Sparkngin is listening on
   * @param Path Path to send message to, i.e. from http://127.0.0.1:7071/message, then Path would be "/message"
   */
  public SparknginMessageForwarder(String Host, int Port, String Path){
    host = Host;
    port = Port;
    path = Path;
    //Start a thread to try and connect to sparkngin
    new Thread() {
      public void run() {
        try{
          for(int i=0; sparknginClient == null && !connected && i<10 ; i++){
            connectToSpark() ;
            if(!connected){
              Thread.sleep(1000);
            }
          }
        }catch (Exception e) {
          e.printStackTrace();
        }
      }
    }.start();
  }
  
  /**
   * Connect to sparkngin and send httpRequest as well as any others that haven't been delivered yet
   * If sparkngin cannot be connected to, then add log to undeliveredReqLog
   * @param log
   */
  protected void forwardToSpark(RequestLog reqLog){
    if(!connected){
      connectToSpark();
    }
    //If unable to connect, store httpRequest in a list and try sending next time
    if(sparknginClient == null || !connected){
      undeliveredReqLog.add(reqLog);
    }
    //Otherwise send all undelivered messages
    else{
      //Only send 100 messages at a time
      //This avoids the possibility of an infinite loop of failure
      int messageAttempts = 0;
      while(!undeliveredReqLog.isEmpty() && messageAttempts < 100){
        sendToSpark(undeliveredReqLog.remove());
        messageAttempts ++;
      }
      sendToSpark(reqLog);
    }
  }
  
  /**
   * Sends data to Sparkngin
   * @param reqLog RequestLog object of HttpRequest to send to Sparkngin
   */
  protected void sendToSpark(RequestLog reqLog){
    String data = JSONSerializer.INSTANCE.toString(reqLog) ;
    //Send request log to sparkngin
    Message message = new Message("id-" + idTracker.incrementAndGet(), data, false) ;
    message.getHeader().setTopic("log.pixel");
    try{
      sparknginClient.post(path, message);
    } 
    catch (Exception e) {
      e.printStackTrace();
      undeliveredReqLog.add(reqLog);
    }
  }
  
  /**
   * Threadsafe method to connect to sparkngin
   */
  protected synchronized void connectToSpark(){
    try {
      sparknginClient = new AsyncHttpClient (host, port, handler) ;
      connected = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Disconnects AsynchHttpClient from sparkngin
   */
  protected void disconnect(){
    try{
      sparknginClient.close();
    }
    catch(Exception e){}
  }
}

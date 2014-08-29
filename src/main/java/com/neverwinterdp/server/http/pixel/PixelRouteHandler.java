package com.neverwinterdp.server.http.pixel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.netty.http.client.AsyncHttpClient;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.UrlParser;

/**
 * @author Richard Duarte
 */
public class PixelRouteHandler extends RouteHandlerGeneric {
  
  //This is a byte array for a 1x1 100% transparent .png image 
  final static ByteBuf IMAGE = Unpooled.wrappedBuffer(
                    new byte[]
                    {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 
                      0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 
                      0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                      0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4, 
                      (byte)0x89, 0x00, 0x00, 0x00, 0x06, 0x62, 0x4B, 0x47,
                      0x44, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xA0,
                      (byte)0xBD, (byte)0xA7, (byte)0x93, 0x00, 0x00, 0x00, 0x09, 0x70, 
                      0x48, 0x59, 0x73, 0x00, 0x00, 0x0B, 0x13, 0x00,
                      0x00, 0x0B, 0x13, 0x01, 0x00, (byte)0x9A, (byte)0x9C, 0x18,
                      0x00, 0x00, 0x00, 0x07, 0x74, 0x49, 0x4D, 0x45,
                      0x07, (byte)0xDE, 0x08, 0x14, 0x14, 0x24, 0x12, 0x12,
                      (byte)0x95, (byte)0xC7, (byte)0xB4, 0x00, 0x00, 0x00, 0x0C, 0x69,
                      0x54, 0x58, 0x74, 0x43, 0x6F, 0x6D, 0x6D, 0x65,
                      0x6E, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xBC,
                      (byte)0xAE, (byte)0xB2, (byte)0x99, 0x00, 0x00, 0x00, 0x10, 0x49,
                      0x44, 0x41, 0x54, 0x08, 0x1D, 0x01, 0x05, 0x00,
                      (byte)0xFA, (byte)0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                      0x05, 0x00, 0x01, (byte)0xBA, (byte)0x89, 0x10, (byte)0x8A, 0x00,
                      0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE,
                      0x42, 0x60, (byte)0x82});
  
  private AtomicLong idTracker = new AtomicLong() ;
  private AsyncHttpClient sparknginClient = null;
  private boolean connectToSpark = false;
  private UrlParser urlParser;
  private DumpResponseHandler handler;
  private Queue<HttpRequest> undeliveredHttpReq;
  
  /**
   * Configure the handler and connection to sparkngin if applicable
   */
  public void configure(Map<String, String> props) {
    String sparknginConnect = props.get("sparkngin.connect") ;
    if(sparknginConnect != null ){
      System.out.println("\n\nSPARKNGIN CONNECT = " + sparknginConnect);
      handler = new DumpResponseHandler() ;
      urlParser = new UrlParser(sparknginConnect) ;
      undeliveredHttpReq = new LinkedList<HttpRequest>();
      connectToSpark = true;
    }
  }
  
  /**
   * Serves IMAGE as mimetype "image/png"
   * If we're supposed to forward these messages to sparkngin, then do so in a thread
   */
  protected void doGet(ChannelHandlerContext ctx, final HttpRequest httpReq) {
    //Return Pixel to client
    this.writeContent(ctx, httpReq, IMAGE, "image/png");
    
    //Forward httpReq to Sparkngin
    forwardToSpark(httpReq);
    /*
    if(connectToSpark){
      new Thread() {
        public void run() {
          try{
            forwardToSpark(httpReq);
          }
            catch (Exception e) {
            e.printStackTrace();
          }
        }
      }.start();
    }
    */
  }
  
  /**
   * Connect to sparkngin and send httpRequest as well as any others that haven't been delivered yet
   * If sparkngin cannot be connected to, then add 
   * @param httpReq
   */
  protected void forwardToSpark(HttpRequest httpReq){
    connectToSpark();
    //If unable to connect, store httpRequest in a list and try sending next time
    if(sparknginClient == null){
      System.err.println("Unable to connect to Sparkngin!");
      undeliveredHttpReq.add(httpReq);
    }
    //Otherwise send all undelivered messages
    else{
      while(!undeliveredHttpReq.isEmpty()){
        sendToSpark(undeliveredHttpReq.remove());
      }
      sendToSpark(httpReq);
      sparknginClient.close();
    }
  }
  
  
  protected void sendToSpark(HttpRequest httpReq){
    try {
      RequestLog log = new RequestLog(httpReq) ;
      String data = JSONSerializer.INSTANCE.toString(log) ;
      //Send request log to sparkngin
      Message message = new Message("id-" + idTracker.incrementAndGet(), data, false) ;
      message.getHeader().setTopic("log.pixel");
      sparknginClient.post("/message", message);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  protected void connectToSpark(){
    //Retry connecting up to 10 times
    for(int i=0; sparknginClient == null && i<10 ; i++){
      try {
        sparknginClient = new AsyncHttpClient (urlParser.getHost(), urlParser.getPort(), handler) ;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public static ByteBuf getImageBytes(){
    return IMAGE;
  }
}
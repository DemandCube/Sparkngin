package com.neverwinterdp.sparkngin.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

import com.codahale.metrics.Timer;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MessageRouteHandler extends RouteHandlerGeneric {
  private MessageForwarderQueue forwarderQueue ;
  private ComponentMonitor monitor ;
  
  public MessageRouteHandler(ApplicationMonitor appMonitor, MessageForwarder forwarder, String queueDir) throws Exception {
    forwarderQueue = new MessageForwarderQueue(appMonitor, forwarder, queueDir) ;
    monitor = appMonitor.createComponentMonitor(getClass()) ;
  }
  
  protected void doPost(ChannelHandlerContext ctx, HttpRequest httpReq) {
    Timer.Context doPostCtx = monitor.timer("doPost()").time() ;
    SendAck ack = new SendAck() ;
    try {
      FullHttpRequest req = (FullHttpRequest) httpReq ;
      ByteBuf byteBuf = req.content() ;
      byte[] data = new byte[byteBuf.readableBytes()] ;
      byteBuf.readBytes(data) ;
      byteBuf.release() ;
      Message message = JSONSerializer.INSTANCE.fromBytes(data, Message.class) ;
      forwarderQueue.put(message);
      ack.setMessageId(message.getHeader().getKey());
      ack.setStatus(SendAck.Status.OK);
    } catch(Throwable t) { 
      ack.setMessage(t.getMessage());
      ack.setStatus(SendAck.Status.ERROR);
      t.printStackTrace(); 
    }
    
    Timer.Context writeOkCtx = monitor.timer("writeOk()").time() ;
    writeJSON(ctx, httpReq, ack) ;
    writeOkCtx.stop() ;
    doPostCtx.stop() ;
  }
}

package com.neverwinterdp.sparkngin.http;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import com.codahale.metrics.Timer;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.route.RouteHandlerGeneric;
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
    writeOK(ctx, httpReq, ack, "application/json") ;
    writeOkCtx.stop() ;
    
    doPostCtx.stop() ;
  }
  
  protected <T> void writeOK(ChannelHandlerContext ctx, HttpRequest req, T obj, String contentType) {
    byte[] data = JSONSerializer.INSTANCE.toBytes(obj) ;
    ByteBuf content = Unpooled.wrappedBuffer(data) ;
    writeOK(ctx, req, content, contentType) ;
  }
  
  protected void writeOK(ChannelHandlerContext ctx, HttpRequest req, ByteBuf content, String contentType) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, OK, content);
    response.headers().set(CONTENT_TYPE, contentType);
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
    response.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
    if(isKeepAlive(req)) {
      response.headers().set(CONNECTION, Values.KEEP_ALIVE);
    }
    Timer.Context timeCtx = monitor.timer("writeAndFlush()").time() ;
    ctx.writeAndFlush(response);
    timeCtx.stop() ;
  }
}

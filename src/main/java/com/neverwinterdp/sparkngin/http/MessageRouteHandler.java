package com.neverwinterdp.sparkngin.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.JSONSerializer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MessageRouteHandler extends RouteHandlerGeneric {
  private Sparkngin sparkngin ;
  
  public MessageRouteHandler(Sparkngin sparkngin)  {
    this.sparkngin = sparkngin ;
  }
  
  protected void doPost(ChannelHandlerContext ctx, HttpRequest httpReq) {
    FullHttpRequest req = (FullHttpRequest) httpReq ;
    ByteBuf byteBuf = req.content() ;
    byte[] data = new byte[byteBuf.readableBytes()] ;
    byteBuf.readBytes(data) ;
    byteBuf.release() ;
    Message message = JSONSerializer.INSTANCE.fromBytes(data, Message.class) ;
    Ack ack = sparkngin.push(message);
    writeJSON(ctx, httpReq, ack) ;
  }
  
  public void close() {
    super.close() ;
  }
}

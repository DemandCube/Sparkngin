package com.neverwinterdp.sparkngin.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.JSONSerializer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class JSONMessageRouteHandler extends RouteHandlerGeneric {
  private Sparkngin sparkngin ;
  
  public JSONMessageRouteHandler(Sparkngin sparkngin)  {
    this.sparkngin = sparkngin ;
  }
  
  @Override
  protected void doGet(ChannelHandlerContext ctx, HttpRequest httpReq) {
    QueryStringDecoder reqDecoder = new QueryStringDecoder(httpReq.getUri()) ;
    String data = reqDecoder.parameters().get("data").get(0) ;
    Message message = JSONSerializer.INSTANCE.fromString(data, Message.class) ;
    Ack ack = sparkngin.push(message);
    writeJSON(ctx, httpReq, ack) ;
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

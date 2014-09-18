package com.neverwinterdp.sparkngin.http;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.IOUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class JBinaryMessageRouteHandler extends RouteHandlerGeneric {
  private Sparkngin sparkngin ;
  
  public JBinaryMessageRouteHandler(Sparkngin sparkngin)  {
    this.sparkngin = sparkngin ;
  }
  
  @Override
  protected void doPost(ChannelHandlerContext ctx, HttpRequest httpReq) {
    FullHttpRequest req = (FullHttpRequest) httpReq ;
    ByteBuf byteBuf = req.content() ;
    byte[] data = new byte[byteBuf.readableBytes()] ;
    byteBuf.readBytes(data) ;
    Ack ack = null ;
    try {
      Message message = (Message) IOUtil.deserialize(data);
      ack = sparkngin.push(message);
    } catch (Exception e) {
      ack = new Ack() ;
      ack.setStatus(Ack.Status.ERROR);
      ack.setMessage(e.getMessage());
      e.printStackTrace();
    }
    try {
      writeBytes(ctx, httpReq, IOUtil.serialize(ack)) ;
    } catch (IOException e) {
      e.printStackTrace();
      this.writeContent(ctx, req, "ERROR: " + e.getMessage(), "text/plain");
    }
  }
  
  public void close() {
    super.close() ;
  }
}

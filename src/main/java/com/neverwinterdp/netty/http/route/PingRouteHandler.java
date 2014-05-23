package com.neverwinterdp.netty.http.route;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class PingRouteHandler extends RouteHandlerGeneric {
  final static public String REPLY_MESSAGE = "Got your message!" ;
  
  protected void doGet(ChannelHandlerContext ctx, HttpRequest httpReq) {
    ByteBuf contentBuf = Unpooled.wrappedBuffer(REPLY_MESSAGE.getBytes()) ;
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, contentBuf);
    response.headers().set(CONTENT_TYPE, "text/plain");
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
    
    boolean keepAlive = isKeepAlive(httpReq);
    if (!keepAlive) {
      ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    } else {
      response.headers().set(CONNECTION, Values.KEEP_ALIVE);
      ctx.write(response);
    }
  }
  
  protected void doPost(ChannelHandlerContext ctx, HttpRequest httpReq) {
    FullHttpRequest req = (FullHttpRequest) httpReq ;
    ByteBuf byteBuf = req.content() ;
    byte[] bytes = new byte[byteBuf.readableBytes()] ;
    byteBuf.readBytes(bytes) ;
    String reply = REPLY_MESSAGE + " - " + new String(bytes) ;
    ByteBuf contentBuf = Unpooled.wrappedBuffer(reply.getBytes()) ;
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, contentBuf);
    response.headers().set(CONTENT_TYPE, "text/plain");
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

    boolean keepAlive = isKeepAlive(httpReq);
    if (!keepAlive) {
      ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    } else {
      response.headers().set(CONNECTION, Values.KEEP_ALIVE);
      ctx.write(response);
    }
  }
}
package com.neverwinterdp.netty.http.route;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED ;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class RouteHandlerGeneric implements RouteHandler {
  protected Logger logger ;
 
  public void setLogger(Logger logger) {
    this.logger = logger ;
  }

  
  final public void handle(ChannelHandlerContext ctx, HttpRequest request) {
    HttpMethod method = request.getMethod() ;
    if(HttpMethod.GET.equals(method)) {
      doGet(ctx, request) ;
    } else if(HttpMethod.POST.equals(method)) {
      doPost(ctx, request) ;
    } else {
      unsupport(ctx, request);
    }
  }
  
  protected void doGet(ChannelHandlerContext ctx, HttpRequest request) {
    unsupport(ctx, request);
  }
  
  protected void doPost(ChannelHandlerContext ctx, HttpRequest request) {
    unsupport(ctx, request); 
  }
  
  protected void unsupport(ChannelHandlerContext ctx, HttpRequest request) {
    String message = "The method " + request.getMethod() + " is not supportted";
    ByteBuf contentBuf = Unpooled.wrappedBuffer(message.getBytes()) ;
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, METHOD_NOT_ALLOWED, contentBuf);
    response.headers().set(CONTENT_TYPE, "text/plain");
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    logger.info(message);
  }
}
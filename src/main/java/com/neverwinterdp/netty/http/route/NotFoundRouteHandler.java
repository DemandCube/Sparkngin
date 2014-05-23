package com.neverwinterdp.netty.http.route;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class NotFoundRouteHandler implements RouteHandler {
  protected Logger logger ;
 
  public void setLogger(Logger logger) {
    this.logger = logger ;
  }

  
  final public void handle(ChannelHandlerContext ctx, HttpRequest request) {
    String message = "Cannot find the handler for " + request.getUri() ;
    ByteBuf contentBuf = Unpooled.wrappedBuffer(message.getBytes()) ;
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, contentBuf);
    response.headers().set(CONTENT_TYPE, "text/plain");
    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    logger.info(message);
  }
}
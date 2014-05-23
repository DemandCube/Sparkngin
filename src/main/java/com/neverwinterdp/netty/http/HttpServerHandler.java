package com.neverwinterdp.netty.http;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;

import org.slf4j.Logger;

import com.neverwinterdp.netty.http.route.RouteHandler;
import com.neverwinterdp.netty.http.route.RouteMatcher;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
  private Logger logger ;
  private RouteMatcher routeMatcher ;
  
  public HttpServerHandler(HttpServer server) {
    logger = server.getLoggerFactory().getLogger(getClass().getSimpleName()) ;
    this.routeMatcher = server.getRouteMatcher() ;
  }
  
  public void add(String path, RouteHandler handler) {
    routeMatcher.addPattern(path, handler) ;
  }
  
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object request) throws Exception {
    if (!(request instanceof HttpRequest)) return ;
    
    HttpRequest httpReq = (HttpRequest) request;
    
    if(is100ContinueExpected(httpReq)) {
      ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
    }
    
    URI uri = new URI(httpReq.getUri()) ;
    String path = uri.getPath() ;
    RouteHandler handler = routeMatcher.findHandler(path) ;
    handler.handle(ctx, httpReq);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}
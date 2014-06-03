package com.neverwinterdp.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import org.slf4j.Logger;

import com.neverwinterdp.netty.http.route.NotFoundRouteHandler;
import com.neverwinterdp.netty.http.route.RouteHandler;
import com.neverwinterdp.netty.http.route.RouteMatcher;
import com.neverwinterdp.util.LoggerFactory;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HttpServer {
  private Logger  logger;
  private int     port = 8080;
  private RouteMatcher routeMatcher = new RouteMatcher() ;
  private Channel channel;
  EventLoopGroup bossGroup, workerGroup ;
  private LoggerFactory loggerFactory = new LoggerFactory() ;
  private Thread deamonThread ;
  
  public int getPort() {
    return this.port;
  }

  public HttpServer setPort(int port) {
    this.port = port;
    return this ;
  }
  
  public LoggerFactory getLoggerFactory() { return this.loggerFactory ; }

  public HttpServer setLoggerFactory(LoggerFactory factory) {
    this.loggerFactory = factory ;
    return this ;
  }
  
  public RouteMatcher getRouteMatcher() { return this.routeMatcher ; }

  public HttpServer add(String path, RouteHandler handler) {
    handler.setLogger(loggerFactory.getLogger(handler.getClass().getSimpleName()));
    routeMatcher.addPattern(path, handler);
    return this ;
  }
  
  public HttpServer setDefault(RouteHandler handler) {
    handler.setLogger(loggerFactory.getLogger(handler.getClass().getSimpleName()));
    routeMatcher.setDefaultHandler(handler);
    return this ;
  }
  
  public void start() throws Exception {
    logger = loggerFactory.getLogger(getClass().getSimpleName()) ;
    logger.info("Start start()");
    if(routeMatcher.getDefaultHandler() == null) {
      setDefault(new NotFoundRouteHandler()) ;
    }
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      ChannelInitializer<SocketChannel> initializer = new ChannelInitializer<SocketChannel>() {
        public void initChannel(SocketChannel ch) throws Exception {
          ChannelPipeline p = ch.pipeline();
          p.addLast("codec", new HttpServerCodec());
          // Remove the following line if you don't want automatic content
          // compression.
          p.addLast("deflater", new HttpContentCompressor());
          p.addLast("aggregator", new HttpObjectAggregator(1048576));
          p.addLast("handler", new HttpServerHandler(HttpServer.this));
        }
      };
      b.option(ChannelOption.SO_BACKLOG, 1024);
      b.group(bossGroup, workerGroup).
        channel(NioServerSocketChannel.class).
        childHandler(initializer);
      channel = b.bind(port).sync().channel();
      logger.info("bind port successfully, channel = " + channel);
      logger.info("Start start() waitting to handle request");
      channel.closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
  
  public void startAsDeamon() {
    deamonThread = new DeamonThread(this) ;
    deamonThread.start() ; 
  }

  public void shutdown() {
    logger.info("Start shutdown()");
    //bossGroup.shutdownGracefully();
    //workerGroup.shutdownGracefully();
    channel.close();
    logger.info("Finish shutdown()");
  }
  
  static public class DeamonThread extends Thread {
    HttpServer instance ;
    
    DeamonThread(HttpServer instance) {
      this.instance = instance ;
    }
    
    public void run() {
      try {
        instance.start();
      } catch (Exception e) {
        instance.logger.error("HttpServer deamon thread has problem.", e);
      }
    }
  }
}
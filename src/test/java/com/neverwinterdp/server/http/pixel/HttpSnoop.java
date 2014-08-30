package com.neverwinterdp.server.http.pixel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import com.neverwinterdp.netty.http.RouteHandlerGeneric;

/**
 * Simple Server to count the number hits are received on POST
 * @author Richard Duarte
 */
public class HttpSnoop extends RouteHandlerGeneric{
  static int hits = 0;

  protected void doPost(ChannelHandlerContext ctx, HttpRequest httpReq) {
    hits++;
  }

  public static int getHits(){
    return hits;
  }
  
  public static void resetHits(){
    hits=0;
  }
}
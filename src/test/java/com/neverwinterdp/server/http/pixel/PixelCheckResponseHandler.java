package com.neverwinterdp.server.http.pixel;

import static org.junit.Assert.assertEquals;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import com.neverwinterdp.netty.http.client.ResponseHandler;

/**
 * Handler to make sure when HTTP response is received,
 * it matches the content served from PixelRouteHandler
 */
public class PixelCheckResponseHandler implements ResponseHandler {
  int count = 0; 
  int failure = 0;
  
  public void onResponse(HttpResponse response) {
    count++;
    HttpContent content = (HttpContent) response;
    ByteBuf buf = content.content();
    try{
      assertEquals(PixelRouteHandler.getImageBytes(), buf);
    }
    catch(AssertionError e){
      failure++;
      e.printStackTrace();
    }
  }
  
  public int getCount(){
    return this.count;
  }
  
  public int getFailure(){
    return this.failure;
  }
  
}
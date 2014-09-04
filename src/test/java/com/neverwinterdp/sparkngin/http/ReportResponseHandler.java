package com.neverwinterdp.sparkngin.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import com.neverwinterdp.netty.http.client.ResponseHandler;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.util.JSONSerializer;

public class ReportResponseHandler implements ResponseHandler {
  int count = 0 ;
  int successCount = 0 ;
  public void onResponse(HttpResponse response) {
    count++ ;
    if(response instanceof HttpContent) {
      HttpContent content = (HttpContent) response;
      String json = content.content().toString(CharsetUtil.UTF_8);
      Ack ack = JSONSerializer.INSTANCE.fromString(json, Ack.class) ;
      if(ack.getStatus().equals(Ack.Status.OK)) successCount++ ;
    }
    if(count % 10000 == 0) {
      System.out.println("count = " + count + ", success count = " + successCount);
    }
  }
}
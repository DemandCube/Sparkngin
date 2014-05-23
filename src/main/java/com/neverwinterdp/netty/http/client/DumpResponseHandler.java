package com.neverwinterdp.netty.http.client;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class DumpResponseHandler implements ResponseHandler {
  private int count  = 0 ;
  
  public int getCount() { return count; }
  
  public void onResponse(HttpResponse response) {
    System.out.println("Message") ;
    System.out.println("--------------------------------------------------------") ;
    printHeaders(response) ;
    printContent(response) ;
    System.out.println("--------------------------------------------------------\n") ;
    count++ ;
  }
  
  private void printHeaders(HttpResponse response) {
    System.out.println("STATUS: " + response.getStatus());
    System.out.println("VERSION: " + response.getProtocolVersion());
    if (!response.headers().isEmpty()) {
      for (String name : response.headers().names()) {
        for (String value : response.headers().getAll(name)) {
          System.out.println("HEADER: " + name + " = " + value);
        }
      }
      System.out.println();
    }
  }
  
  private void printContent(HttpResponse response) {
    if(response instanceof HttpContent) {
      HttpContent content = (HttpContent) response;
      System.out.print(content.content().toString(CharsetUtil.UTF_8));
      System.out.flush();
      if (content instanceof LastHttpContent) {
        System.out.println();
      }
    }
  }
}

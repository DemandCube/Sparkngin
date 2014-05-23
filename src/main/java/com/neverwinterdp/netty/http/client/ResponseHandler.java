package com.neverwinterdp.netty.http.client;

import io.netty.handler.codec.http.HttpResponse;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public interface ResponseHandler {
  public void onResponse(HttpResponse response) ;
}

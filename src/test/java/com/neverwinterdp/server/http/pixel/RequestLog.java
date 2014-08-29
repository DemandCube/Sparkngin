package com.neverwinterdp.server.http.pixel;

import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RequestLog {
  private String id ;
  
  private String uri ;
  private String method ;
  private Map<String, String> requestHeaders ;
  
  public RequestLog() { }
  
  public RequestLog(HttpRequest httpReq) {
    this.uri = httpReq.getUri() ;
    this.method = httpReq.getMethod().name() ;
    requestHeaders = new HashMap<String, String>() ;
    Iterator<Entry<String, String>> i = httpReq.headers().iterator() ;
    while(i.hasNext()) {
      Entry<String, String> entry =i.next();
      requestHeaders.put(entry.getKey(), entry.getValue()) ;
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, String> getRequestHeaders() {
    return requestHeaders;
  }

  public void setRequestHeaders(Map<String, String> requestHeaders) {
    this.requestHeaders = requestHeaders;
  }
}

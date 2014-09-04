package com.neverwinterdp.sparkngin.http;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.server.service.ServiceInfo;

public class SparknginClusterHttpServiceInfo extends ServiceInfo {
  
  @Inject @Named("sparkngin:http-listen-port")
  private int httpListenPort = 8080 ;
  
  @Inject(optional = true) @Named("sparkngin:http-www-dir")
  private String wwwDir = null;
  
  public int getHttpListenPort() { return httpListenPort; }

  public String getWwwDir() { return wwwDir; }
}
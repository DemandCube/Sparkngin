package com.neverwinterdp.sparkngin.http;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.server.service.ServiceInfo;

public class SparknginHttpConnectorServiceInfo extends ServiceInfo {
  
  @Inject @Named("sparkngin:http-listen-port")
  private int httpListenPort = 8080 ;
  
  @Inject(optional = true) @Named("sparkngin:http-www-dir")
  private String wwwDir = null;
  
  @Inject @Named("sparknginProperties") 
  private Map<String, String> properties ;
  
  public int httpListenPort() { return httpListenPort; }

  public String wwwDir() { return wwwDir; }
  
  public Map<String, String> getProperties() { return this.properties ; }
}
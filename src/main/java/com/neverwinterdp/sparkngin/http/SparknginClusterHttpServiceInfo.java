package com.neverwinterdp.sparkngin.http;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.server.module.ModuleProperties;
import com.neverwinterdp.server.service.ServiceInfo;

public class SparknginClusterHttpServiceInfo extends ServiceInfo {
  
  @Inject(optional = true) @Named("sparkngin:http-listen-port")
  private int httpListenPort = 8080 ;
  
  @Inject(optional = true) @Named("sparkngin:http-www-dir")
  private String wwwDir = null;
  
  @Inject(optional = true) @Named("sparkngin:queue-dir")
  private String queueDir ;

  @Inject(optional = true) @Named("sparkngin:forwarder-class")
  private String forwarderClass = NullDevMessageForwarder.class.getName() ;
  
  @Inject
  public void init(ModuleProperties moduleProperties) {
    if(queueDir == null) {
      queueDir = moduleProperties.getDataDir() + "/sparkngin/queue" ;
    }
  }
  
  public int getHttpListenPort() { return httpListenPort; }

  public String getForwarderClass() { return this.forwarderClass ; }
  
  public String getWwwDir() { return wwwDir; }

  public String getQueueDir() { return queueDir; }
  
}
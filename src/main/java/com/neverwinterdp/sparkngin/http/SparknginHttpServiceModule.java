package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.server.service.ServiceModule;

public class SparknginHttpServiceModule extends ServiceModule {
  @Override
  protected void configure() {  
    bind("SparknginClusterHttoService", SparknginClusterHttpService.class) ;
  }
}
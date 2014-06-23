package com.neverwinterdp.server.module;

import java.util.Map;

import com.neverwinterdp.server.module.ServiceModule;
import com.neverwinterdp.sparkngin.http.SparknginClusterHttpService;

@ModuleConfig(name = "Sparkngin", autostart = false, autoInstall=false)
public class SparknginModule extends ServiceModule {
  
  protected void configure(Map<String, String> properties) {  
    bind("SparknginClusterHttpService", SparknginClusterHttpService.class) ;
  }

}
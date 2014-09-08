package com.neverwinterdp.server.module;

import java.util.Map;

import com.neverwinterdp.sparkngin.http.SparknginHttpService;

@ModuleConfig(name = "Sparkngin", autostart = false, autoInstall=false)
public class SparknginModule extends ServiceModule {
  
  protected void configure(Map<String, String> properties) {  
    bindService(SparknginHttpService.class) ;
  }
}
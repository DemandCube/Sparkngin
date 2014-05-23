package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;

public interface MessageForwarder {
  public void onInit() ;
  public void onDestroy() ;
  public void forward(Message message) throws Exception ;
}
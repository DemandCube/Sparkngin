package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;

public interface MessageForwarder {
  public void forward(Message message) throws Exception ;
  public void close() ;
}
package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;

public interface MessageForwarder {
  public boolean hasError() ;
  public void    setError(Throwable error) ;
  public boolean reconnect() ;
  public void forward(Message message) throws Exception ;
  public void close() ;
}
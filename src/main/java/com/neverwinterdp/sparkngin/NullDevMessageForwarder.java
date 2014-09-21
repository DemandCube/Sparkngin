package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;

public class NullDevMessageForwarder implements MessageForwarder {
  private int count ;
  
  public NullDevMessageForwarder() {
  }
  
  public int getProcessCount() { return count ; }
  
  public void forward(Message message) {
    count++ ;
    dump(message) ;
  }
  
  public void dump(Message message) {
  }
  
  public void close() {
  }
}

package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;

public class NullDevMessageForwarder implements MessageForwarder {
  private int count ;
  
  public void onInit() {
  }

  public void close() {
  }
  
  public int getProcessCount() { return count ; }
  
  public void reset() { 
    count = 0 ; 
  }
  
  public void forward(Message message) {
    count++ ;
    if(count % 100 == 0) {
      //System.out.println("Receive " + count);
    }
  }
}

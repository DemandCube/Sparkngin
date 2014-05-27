package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;

public class NullDevMessageForwarder implements MessageForwarder {
  private int count ;
  
  public void onInit() {
  }

  public void onDestroy() {
  }
  
  public int getProcessCount() { return count ; }
  
  public void forward(Message message) {
    count++ ;
    if(count % 100 == 0) {
      //System.out.println("Receive " + count);
    }
  }
}

package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;

public class DevNullMessageForwarder implements MessageForwarder {
  private int count ;
  
  public void onInit() {
  }

  public void onDestroy() {
  }
  
  public int getProcessCount() { return count ; }
  
  public void forward(Message message) {
    count++ ;
  }
}

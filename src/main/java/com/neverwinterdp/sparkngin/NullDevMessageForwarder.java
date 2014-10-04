package com.neverwinterdp.sparkngin;

import java.util.Map;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.MapUtil;

public class NullDevMessageForwarder implements MessageForwarder {
  private boolean dump = false ;
  private int count ;
  
  
  public NullDevMessageForwarder() {
  }
  
  public NullDevMessageForwarder(Map<String, String> props) {
    dump = MapUtil.getBool(props, "forwarder.nulldev.dump", false) ;
  }
  
  public int getProcessCount() { return count ; }
  
  public void forward(Message message) {
    count++ ;
    dump(message) ;
  }
  
  public void dump(Message message) {
    if(!dump) return ;
    System.out.println(JSONSerializer.INSTANCE.toString(message.getHeader()));
    try {
      Class<?> type = Class.forName(message.getData().getType()) ;
      Object object = message.getData().getDataAs(type) ;
      System.out.println(JSONSerializer.INSTANCE.toString(object));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  public void close() {
  }
}

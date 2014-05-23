package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public interface SparknginSimpleClient {
  public String getConnectionUrl() ;  
  public void send(String topic, Message message, SendMessageHandler handler) ;
}
package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginClient {
  private int currentIdx = 0;
  private SparknginSimpleClient[] client ;
  
  public SparknginClient(SparknginSimpleClient[] client) {
    this.client = client; 
  }
  
  public void send(String topic, Message message, SendMessageHandler handler) {
    //TODO: if the clusterClient fail to send, remove the clusterClient from the list
    //      retry another one
    SparknginSimpleClient client = next() ;
    client.send(topic, message, handler) ;
  }
  
  synchronized SparknginSimpleClient next() {
    if(currentIdx == client.length) currentIdx = 0;
    return client[currentIdx++] ;
  }
}

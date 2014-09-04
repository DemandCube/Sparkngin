package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class ClusterSparknginClient {
  private int currentIdx = 0;
  private SparknginClient[] client ;
  
  public ClusterSparknginClient(SparknginClient[] client) {
    this.client = client; 
  }
  
  public void send(String topic, Message message, SendMessageHandler handler) {
    //TODO: if the clusterClient fail to send, remove the clusterClient from the list
    //      retry another one
    SparknginClient client = next() ;
    client.send(topic, message, handler) ;
  }
  
  synchronized SparknginClient next() {
    if(currentIdx == client.length) currentIdx = 0;
    return client[currentIdx++] ;
  }
}

package com.neverwinterdp.sparkngin;

import com.neverwinterdp.message.Message;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public interface SendMessageHandler {
  public void onResponse(Message message, SparknginClient client, Ack ack) ;
  public void onError(Message message, SparknginClient client, Throwable error) ;
  public void onRetry(Message message, SparknginClient client) ;
}

package com.neverwinterdp.sparkngin.http;

import io.netty.handler.codec.http.HttpContent;
import io.netty.util.CharsetUtil;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.util.JSONSerializer;

public class JSONHttpSparknginClient extends AbstractHttpSparknginClient {
  
  public JSONHttpSparknginClient(String host, int port, int bufferSize, boolean connect) throws Exception {
    super(host, port, bufferSize, connect) ;
    setPath("/message/json") ;
  }
  
  protected Ack toAck(HttpContent content) {
    String json = content.content().toString(CharsetUtil.UTF_8);
    Ack ack = JSONSerializer.INSTANCE.fromString(json, Ack.class) ;
    return ack ;
  }


  @Override
  protected byte[] toBinData(Message message) {
    return JSONSerializer.INSTANCE.toBytes(message) ;
  }

  protected String toStringData(Message message) {
    return JSONSerializer.INSTANCE.toString(message);
  }

}

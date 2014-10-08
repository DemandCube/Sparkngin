package com.neverwinterdp.sparkngin.http;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.Ack;
import com.neverwinterdp.util.IOUtil;
import com.neverwinterdp.util.JSONSerializer;

public class JBinaryHttpSparknginClient extends AbstractHttpSparknginClient {
  public JBinaryHttpSparknginClient(String host, int port, int bufferSize, boolean connect) throws Exception {
    super(host, port, bufferSize, connect) ;
    setPath("/message/jbinary") ;
  }
  
    
  protected Ack toAck(HttpContent content) {
    ByteBuf byteBuf = content.content() ;
    byte[] data = new byte[byteBuf.readableBytes()] ;
    byteBuf.readBytes(data) ;
    //byteBuf.release() ;
    Ack ack = null;
    try {
      ack = (Ack)IOUtil.deserialize(data);
    } catch (Exception e) {
      e.printStackTrace();
      ack = new Ack() ;
      ack.setStatus(Ack.Status.ERROR);
      ack.setMessage(e.getMessage());
    }
    return ack ;
  }


  @Override
  protected byte[] toBinData(Message message) {
    try {
      return IOUtil.serialize(message) ;
    } catch (IOException e) {
      throw new RuntimeException(e) ;
    }
  }

  protected String toStringData(Message message) {
    return JSONSerializer.INSTANCE.toString(message);
  }

}

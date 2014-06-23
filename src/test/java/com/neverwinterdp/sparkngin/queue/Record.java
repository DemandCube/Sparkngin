package com.neverwinterdp.sparkngin.queue;

import java.io.IOException;
import java.io.Serializable;

import com.neverwinterdp.util.IOUtil;

public class Record<T extends Serializable> implements Serializable {
  private byte[] data ;
  
  public Record() {
    
  }
  
  public Record(T object) throws IOException {
    if(object == null) return ;
    data = IOUtil.serialize(object) ;
  }
  
  public T getObject() throws Exception {
    if(data == null) return null ;
    return (T) IOUtil.deserialize(data) ;
  }
  
  public byte[] getData() { return data ; }
  public void   setData(byte[] data) {
    this.data = data ;
  }
}

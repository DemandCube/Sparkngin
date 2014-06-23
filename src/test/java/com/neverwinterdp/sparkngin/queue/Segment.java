package com.neverwinterdp.sparkngin.queue;

import java.io.IOException;
import java.io.Serializable;

import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;

public class Segment<T extends Serializable> {
  transient private IndexedChronicle chronicle ;
  transient private ExcerptTailer    reader ;
  transient private  ExcerptAppender writer ;
  
  private String storeDir ;
  private int  index ;
  private long maxSize = 1024 * 1024 ;
  
  public Segment(String storeDir, int idx) {
    this.storeDir = storeDir ;
    this.index = idx ;
  }

  public int getIndex() { return index; }
  public void setIndex(int index) { this.index = index; }

  public boolean isFull() throws Exception {
    return chronicle.size() > maxSize;
  }
  
  public boolean hasNext() throws Exception {
    if(reader == null) reader = chronicle.createTailer();
    return reader.nextIndex() ;
  }
  
  public T next() throws Exception {
    if(reader == null) reader = chronicle.createTailer();
    Record<T> record = (Record<T>)reader.readObject(Record.class) ;
    return record.getObject() ;
  }
  
  public void append(T object) throws Exception {
    if(isFull()) throw new IllegalStateException("The segment is full") ;
    if(writer == null) writer = chronicle.createAppender();
    writer.startExcerpt();
    Record<T> record = new Record<T>(object) ;
    writer.writeObject(record);
    writer.finish();
  }
  
  public void delete() throws Exception {
    close() ;
  }
  
  public void open() throws Exception {
    chronicle = new IndexedChronicle(storeDir + "/segment-" + index) ;
  }
  
  public void close() throws IOException {
    if(chronicle != null) {
      writer.close();
      chronicle.close() ;
      chronicle = null ;
    }
  }
}
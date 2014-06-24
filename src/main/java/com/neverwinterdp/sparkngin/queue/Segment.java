package com.neverwinterdp.sparkngin.queue;

import java.io.IOException;
import java.io.Serializable;

import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.tools.ChronicleTools;

import com.neverwinterdp.util.IOUtil;

public class Segment<T extends Serializable> {
//16 billion max, or one per day for 11 years.
  public static final ChronicleConfig TINY = new ChronicleConfig(1 * 1024, 2 * 1024 * 1024, true, 4 * 1024 * 1024);
  
  private IndexedChronicle chronicle ;
  private ExcerptTailer    reader ;
  private ExcerptAppender  appender ;
  
  private String storeDir ;
  private int  segmentIndex ;
  private long maxSize = 8 * 1024;
  
  public Segment(String storeDir, int idx, long maxSize) {
    this.maxSize = maxSize ;
    this.storeDir = storeDir ;
    this.segmentIndex = idx ;
  }

  public int getSegmentIndex() { return segmentIndex; }
  public void setSegmentIndex(int index) { this.segmentIndex = index; }

  public boolean isFull() throws Exception {
    return appender.size() > maxSize;
  }
  
  public boolean hasNext() throws Exception {
    return reader.nextIndex() ;
  }
  
  public T next() throws Exception {
    int len = reader.readInt() ;
    byte[] data =  new byte[len] ;
    reader.read(data) ;
    T object = (T) IOUtil.deserialize(data) ;
    return object ;
  }
  
  public void append(T object) throws Exception {
    appender.startExcerpt();
    byte[] data = IOUtil.serialize(object) ;
    appender.writeInt(data.length);
    appender.write(data);
    appender.finish();
  }
  
  public void delete() throws Exception {
    close() ;
    ChronicleTools.deleteOnExit(storeDir + "/segment-" + segmentIndex);
  }
  
  public void open() throws Exception {
    if(chronicle != null) return ;
    chronicle = new IndexedChronicle(storeDir + "/segment-" + segmentIndex, TINY) ;
    appender = chronicle.createAppender();
    reader = chronicle.createTailer() ;
  }
  
  public void close() throws IOException {
    if(chronicle != null) {
      appender.close();
      reader.close();
      chronicle.close() ;
      chronicle = null ;
    }
  }
}
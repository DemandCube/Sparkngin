package com.neverwinterdp.sparkngin.queue;

import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.Excerpt;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;

import org.junit.Test;

import com.neverwinterdp.util.FileUtil;

public class ChronicleUnitTest {
  @Test
  public void testAppender() throws Exception {
    String dataDir = "build/chronicle";
    FileUtil.removeIfExist(dataDir, true);
    System.out.println("data dir: " + dataDir);
    IndexedChronicle chronicle = new IndexedChronicle(dataDir + "/data", ChronicleConfig.SMALL);
    ExcerptAppender appender = chronicle.createAppender();
    appender.startExcerpt(8);
    appender.writeLong(10l);
    appender.finish();
    
    for(int i = 0; i < 10; i++) {
      String string = "test " + i ;
      byte[] data = string.getBytes() ;
      appender.startExcerpt(data.length);
      appender.write(data);
      appender.finish();
    }
    
    ExcerptTailer reader = chronicle.createTailer();
    reader.index(1) ;
    while (reader.nextIndex()) {
      System.out.println("Read string by reader: " + reader.readLine());
    }
    
    Excerpt excerpt = chronicle.createExcerpt() ;
    excerpt.index(1);
    while(excerpt.nextIndex()) {
      System.out.println("Read string by exerpt: " + excerpt.readLine());
    }
    
    appender.close();
    reader.close();
    excerpt.close();
    
    excerpt = chronicle.createExcerpt() ;
    long longNumber = excerpt.readLong() ;
    System.out.println("Long number = " + longNumber);
  }
}
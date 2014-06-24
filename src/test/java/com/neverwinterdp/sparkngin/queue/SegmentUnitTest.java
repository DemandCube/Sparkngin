package com.neverwinterdp.sparkngin.queue;

import org.junit.Test;

import com.neverwinterdp.util.FileUtil;

public class SegmentUnitTest {
  @Test
  public void testSegment() throws Exception {
    String segmentDir = "build/segment" ;
    FileUtil.removeIfExist(segmentDir, false);
    Segment<String> segment = new Segment<String>(segmentDir, 0, 10000) ;
    segment.open();
    for(int i = 0; i < 10; i++) {
      segment.append("This is a test , This is a test, This is a test" + i);
    }
    segment.close();
    
    segment.open();
    int read = 0 ;
    while(segment.hasNext() && read < 5) {
      String text = segment.next() ;
      System.out.println("text: " + text);
      read++ ;
    }
    segment.close();
  }
}

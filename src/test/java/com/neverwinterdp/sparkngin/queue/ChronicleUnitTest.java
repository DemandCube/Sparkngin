package com.neverwinterdp.sparkngin.queue;

import java.io.IOException;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;

public class ChronicleUnitTest {
  public static void writeToChronicle(Chronicle chr, String someString) throws IOException {
    ExcerptAppender appender = chr.createAppender();
    appender.startExcerpt();
    appender.writeBytes(someString);
    appender.finish();
  }

  public static void dumpChronicle(Chronicle chr) throws IOException {
    ExcerptTailer excerpt = chr.createTailer();
    while (excerpt.nextIndex()) {
      System.out.println("Read string from chronicle: " + excerpt.readLine());
    }
  }

  public static void main(String[] args) {
    try {
      String basePrefix = "build/chronicle/data";
      System.out.println("base prefix: " + basePrefix);
      Chronicle chronicle = new IndexedChronicle(basePrefix);

      writeToChronicle(chronicle, "Some text");
      writeToChronicle(chronicle, "more text");
      writeToChronicle(chronicle, "and a little bit more");

      dumpChronicle(chronicle);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
package com.neverwinterdp.sparkngin.http;

import org.junit.Test;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.util.IOUtil;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.yara.MetricPrinter;
import com.neverwinterdp.yara.MetricRegistry;
import com.neverwinterdp.yara.Timer;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SerializationPerformanceTest {
  @Test
  public void testPerformance() throws Exception {
    int LOOP = 1000000 ;
    System.out.println("Test default java serialization") ;
    testJavaSerialization(LOOP) ;
    System.out.println("\n\n");
    System.out.println("Test JSON serialization") ;
    testJSONSerialization(LOOP) ;
  }
  
  void testJavaSerialization(int loop) throws Exception {
    MetricRegistry mRegistry = new MetricRegistry() ;
    Message message = new Message("message", new byte[1024], true) ;
    byte[] data = IOUtil.serialize(message) ;
    long start = System.currentTimeMillis() ;
    for(int i = 0; i < loop; i++) {
      Timer.Context serializeCtx = mRegistry.timer("serialize").time() ;
      IOUtil.serialize(message) ;
      serializeCtx.stop() ;
      
      Timer.Context deserializeCtx = mRegistry.timer("deserialize").time() ;
      IOUtil.deserialize(data) ;
      deserializeCtx.stop() ;
    }
    long stop = System.currentTimeMillis() ;
    new MetricPrinter().print(mRegistry);
    System.out.println("Serialize/Deserialze " + loop + " in " + (stop -start) + "ms");
  }
  
  void testJSONSerialization(int loop) throws Exception {
    MetricRegistry mRegistry = new MetricRegistry() ;
    Message message = new Message("message", new byte[1024], true) ;
    byte[] data = JSONSerializer.INSTANCE.toBytes(message) ;
    long start = System.currentTimeMillis() ;
    for(int i = 0; i < loop; i++) {
      Timer.Context serializeCtx = mRegistry.timer("serialize").time() ;
      JSONSerializer.INSTANCE.toBytes(message) ;
      serializeCtx.stop() ;
      
      Timer.Context deserializeCtx = mRegistry.timer("deserialize").time() ;
      JSONSerializer.INSTANCE.fromBytes(data, Message.class) ;
      deserializeCtx.stop() ;
    }
    long stop = System.currentTimeMillis() ;
    new MetricPrinter().print(mRegistry);
    System.out.println("Serialize/Deserialze " + loop + " in " + (stop -start) + "ms");
  }
}

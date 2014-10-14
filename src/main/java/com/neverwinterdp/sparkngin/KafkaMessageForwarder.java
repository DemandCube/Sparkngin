package com.neverwinterdp.sparkngin;

import java.util.Map;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.yara.MetricRegistry;

public class KafkaMessageForwarder implements MessageForwarder {
  private Logger logger ;
  private KafkaMessageProducer producer ;
  private Throwable error ;
  
  public KafkaMessageForwarder() {}
  
  public KafkaMessageForwarder(LoggerFactory lfactory,
                               MetricRegistry mRegistry,Map<String, String> props) {
    init(lfactory, mRegistry, props) ;
  }
  
  @Inject
  public void init(LoggerFactory lfactory,
                   MetricRegistry mRegistry, 
                   @Named("kafka-producerProperties") Map<String, String> producerProps) {
    logger = lfactory.getLogger(KafkaMessageForwarder.class) ;
    logger.info("Start init()");
    logger.info("Kafka Producer Properties: \n" + JSONSerializer.INSTANCE.toString(producerProps));
    producer = new KafkaMessageProducer(producerProps, mRegistry, "127.0.0.1:9092") ;
    logger.info("Finish init()");
  }
  
  public boolean hasError() { return error != null ; }
  
  public void setError(Throwable error) { this.error = error ; }
  
  public boolean reconnect() { 
    error = null ;
    producer.reconnect();
    return true ; 
  }
  
  public void forward(Message message) throws Exception {
    String topic = message.getHeader().getTopic() ;
    producer.send(topic, message);
  }
  
  public void close() {
    logger.info("Start close()");
    producer.close() ;
    logger.info("Finish close()");
  }
}
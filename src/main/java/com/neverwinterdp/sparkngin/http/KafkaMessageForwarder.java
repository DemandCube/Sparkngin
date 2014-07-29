package com.neverwinterdp.sparkngin.http;

import java.util.Map;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;

public class KafkaMessageForwarder implements MessageForwarder {
  private Logger logger ;
  private KafkaMessageProducer producer ;

  @Inject
  public void init(LoggerFactory lfactory,
                   ApplicationMonitor appMonitor, 
                   @Named("kafka-producerProperties") Map<String, String> producerProps,
                   @Named("kafka-producer:metadata.broker.list") String brokerList) {
    logger = lfactory.getLogger(KafkaMessageForwarder.class) ;
    logger.info("Start init()");
    ComponentMonitor monitor = appMonitor.createComponentMonitor(KafkaMessageProducer.class) ;
    logger.info("Kafka Producer Properties: \n" + JSONSerializer.INSTANCE.toString(producerProps));
    producer = new KafkaMessageProducer(producerProps, monitor, brokerList) ;
    logger.info("Finish init()");
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

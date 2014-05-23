package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;

public class KafkaMessageForwarder implements MessageForwarder {
  private KafkaMessageProducer producer ;
  private String kafkaBrokerList = "127.0.0.1:9092" ;

  public void setKafkaBrokerList(String brokerList) {
    this.kafkaBrokerList = brokerList ;
  }
  
  public void onInit() {
    producer = new KafkaMessageProducer(kafkaBrokerList) ;
  }
  
  public void onDestroy() {
    producer.close() ;
  }
  
  public void forward(Message message) throws Exception {
    String topic = message.getHeader().getTopic() ;
    producer.send(topic, message);
  }
}

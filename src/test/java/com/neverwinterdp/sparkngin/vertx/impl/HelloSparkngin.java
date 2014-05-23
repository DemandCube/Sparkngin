package com.neverwinterdp.sparkngin.vertx.impl;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.message.SampleEvent;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.sparkngin.SendMessageHandler;
import com.neverwinterdp.sparkngin.SparknginClient;
import com.neverwinterdp.sparkngin.SparknginSimpleClient;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HelloSparkngin {
  static String getValue(String arg) {
    String[] array = arg.split("=", 2) ;
    return array[1] ;
  }
  
  static public void main(String[] args) throws Exception {
    int numOfMessages = 10 ;
    String topic = "HelloSparkngin" ;
    String[] connectionUrl = { "http://127.0.0.1:8080"};
    System.out.println("Available options: ");
    System.out.println("  --topic=topic");
    System.out.println("  --num-of-messages=1000");
    System.out.println("  --urls=127.0.0.1:8080,127.0.0.1:8081");
    
    if(args != null) {
      for(String arg : args) {
        if(arg.startsWith("--topic")) topic = getValue(arg) ;
        else if(arg.startsWith("--num-of-messages")) numOfMessages = Integer.parseInt(getValue(arg)) ;
        else if(arg.startsWith("--urls")) {
          String urls = getValue(arg) ;
          connectionUrl = urls.split(",") ;
        } else {
          System.out.println("Unknown option: " + arg);
          return ;
        }
      }
    }
    
    SendMessageHandler sendHandler = new SendMessageHandler() {
      public void onResponse(Message msg, SparknginSimpleClient client, SendAck ack) {
        SampleEvent event =  msg.getData().getDataAs(SampleEvent.class) ;
        System.out.println("Ack: " + event.getDescription() + " " + ack.getStatus()) ;
      }

      public void onError(Message message, SparknginSimpleClient client, Throwable error) {
      }

      public void onRetry(Message message, SparknginSimpleClient client) {
      }
    };
    
    System.out.println("Start sending " + numOfMessages + " to topic " + topic);
    SparknginSimpleClient[] simpleClients = SparknginSimpleVertxHttpClient.create(connectionUrl) ;
    SparknginClient client = new SparknginClient(simpleClients) ; ;
    for(int i = 0; i < numOfMessages; i++) {
      SampleEvent event = new SampleEvent("event-" + i, "event " + i) ;
      Message message = new Message("m" + i, event, true) ;
      client.send(topic, message, sendHandler) ;
      System.out.println("Send " + event.getDescription());
    } 
    System.out.println("Please wait, if your message are sent successfully, you should see OK Ack, if not Error Ack") ;
    Thread.currentThread().join();
  }
}

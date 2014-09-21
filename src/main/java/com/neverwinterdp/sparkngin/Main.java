package com.neverwinterdp.sparkngin;

import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.HttpServer;
import com.neverwinterdp.sparkngin.http.JBinaryMessageRouteHandler;
import com.neverwinterdp.sparkngin.http.JSONMessageRouteHandler;
import com.neverwinterdp.sparkngin.http.TrackingPixelRouteHandler;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.LoggerFactory;
import com.neverwinterdp.yara.MetricRegistry;

public class Main {
  static public class Options {
    @Parameter(names = "--data-dir", description = "Data directory")
    String dataDir = "./data" ;
    
    @Parameter(names = "--forwarder", description = "The forwarder, either nulldev or kafka")
    String forwarder = "nulldev" ;
    
    @Parameter(names = "--http-listen-port", description = "Server http listen port")
    int httpListenPort = 7080;
    
    @DynamicParameter(names = "--kafka:", description = "Kafka producer parameters")
    private Map<String, String> kafkaParams = new HashMap<String, String>();
    
    @DynamicParameter(names = "--sparkngin:", description = "Sparkngin properties")
    private Map<String, String> sparknginParams = new HashMap<String, String>();
  }

  static public void main(String[] args) throws Exception {
    Options options = new Options() ;
    JCommander jcommander = new JCommander(options, args) ;
    jcommander.usage(); 
    
    LoggerFactory lfactory = new LoggerFactory() ;
    MetricRegistry mRegistry = new MetricRegistry() ;
    
    HttpServer server = new HttpServer() ;
    server.setLoggerFactory(lfactory) ;
    server.setPort(options.httpListenPort) ;
    
    MessageForwarder forwarder = null ;
    if("kafka".equals(options.forwarder)) {
      forwarder = new KafkaMessageForwarder(lfactory, mRegistry, options.kafkaParams) ;
    } else {
      forwarder = new NullDevMessageForwarder() {
        public void dump(Message message) {
          System.out.println(JSONSerializer.INSTANCE.toString(message.getHeader()));
          try {
            Class<?> type = Class.forName(message.getData().getType()) ;
            Object object = message.getData().getDataAs(type) ;
            System.out.println(JSONSerializer.INSTANCE.toString(object));
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        }
      };
    }
    Sparkngin sparkngin = new Sparkngin(mRegistry, forwarder, options.dataDir) ;
    server.add("/message/json", new JSONMessageRouteHandler(sparkngin));
    server.add("/message/jbinary", new JBinaryMessageRouteHandler(sparkngin));
    server.add("/tracking/site/:site", new TrackingPixelRouteHandler(sparkngin, options.sparknginParams));
    server.startAsDeamon() ;
    Thread.currentThread().join();
  }
}
package com.neverwinterdp.server.shell;

import com.beust.jcommander.Parameter;
import com.neverwinterdp.message.Message;
import com.neverwinterdp.sparkngin.http.JSONHttpSparknginClient;

public class HelloSparkngin {
  static public class Options {
    @Parameter(names = "-host", description = "Server host name or ip")
    String host = "127.0.0.1";
    
    @Parameter(names = "-port", description = "Server listen port")
    int port = 8080;
    
    @Parameter(names = {"-topic", "--topic"}, description = "Topic name")
    String topic = "metrics.consumer";
  
    @Parameter(
        names = {"-num-message", "--num-message"}, 
        description = "Number of message to generate"
     )
    int numMessage = 30000 ;
  }

  public void run(final Options options) throws Exception {
    final JSONHttpSparknginClient client = new JSONHttpSparknginClient (options.host, options.port,300) ;
    for(int i = 0; i < options.numMessage; i++) {
      Message message = new Message("m" + i, new byte[1024], true) ;
      message.getHeader().setTopic(options.topic);
      client.sendPost(message, 5000);
      if(i > 0 && i % 10000 == 0) {
        System.out.print(".");
      }
    }
    client.waitAndClose(30000);
    System.out.println("\nDone!!!!");
  }
}
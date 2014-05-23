package com.neverwinterdp.sparkngin.http;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.client.DumpResponseHandler;
import com.neverwinterdp.netty.http.client.HttpClient;
import com.neverwinterdp.sparkngin.SendMessageHandler;
import com.neverwinterdp.sparkngin.SparknginSimpleClient;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class SparknginSimpleHttpClient implements SparknginSimpleClient {
  private String host ;
  private int    port ;
  private String connectionUrl ;
  private HttpClient client ;
  
  public SparknginSimpleHttpClient(String connectionUrl) throws Exception {
    this.connectionUrl = connectionUrl ;
    if(connectionUrl.startsWith("http://")) {
      connectionUrl = connectionUrl.substring(7) ;
    }
    String[] part = connectionUrl.split(":") ;
    this.host = part[0] ;
    this.port = Integer.parseInt(part[1]) ;
    DumpResponseHandler handler = new DumpResponseHandler() ;
    client = new HttpClient(host, port, handler) ;
  }
  
  public String getConnectionUrl() { return this.connectionUrl ; }
  
  public void send(String topic, final Message message, final SendMessageHandler mHandler) {
  }
  
  static public SparknginSimpleClient[] create(String[] connectionUrls) {
    SparknginSimpleClient[] instances = new SparknginSimpleClient[connectionUrls.length] ;
    for(int i = 0; i < instances.length; i++) {
    }
    return instances; 
  }
}
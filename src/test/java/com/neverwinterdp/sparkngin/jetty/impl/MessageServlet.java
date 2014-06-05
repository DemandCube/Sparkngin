package com.neverwinterdp.sparkngin.jetty.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.queuengin.kafka.KafkaMessageProducer;
import com.neverwinterdp.sparkngin.SendAck;
import com.neverwinterdp.util.IOUtil;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.monitor.ApplicationMonitor;
import com.neverwinterdp.util.monitor.ComponentMonitor;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class MessageServlet extends HttpServlet {
  private KafkaMessageProducer producer ;
  
  public void init(ServletConfig config) throws ServletException {
    super.init(config) ;
    String kafkaConnectionUrls = "127.0.0.1:9090,127.0.0.1:9091" ; 
    ApplicationMonitor appMonitor = new ApplicationMonitor() ;
    ComponentMonitor monitor = appMonitor.createComponentMonitor(KafkaMessageProducer.class) ;
    producer = new KafkaMessageProducer(monitor, kafkaConnectionUrls) ;
  }
  
  protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    SendAck ack = new SendAck() ;
    try {
      String topic = getTopic(req) ;
      InputStream is = req.getInputStream() ;
      String json = IOUtil.getStreamContentAsString(is, "UTF-8") ;
      Message message = JSONSerializer.INSTANCE.fromString(json, Message.class) ;
      if(message.getHeader().isTraceEnable()) {
        req.getLocalPort() ;
        message.addTrace("JSONMessageServlet", "forward by http server, ip = " + req.getLocalAddr() + ", port " + req.getLocalPort()) ;
      }
      producer.send(topic, message) ;
      ack.setStatus(SendAck.Status.OK) ;
    } catch (Exception e) {
      ack.setStatus(SendAck.Status.ERROR) ;
      ack.setMessage(e.getMessage()) ;
    }
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getOutputStream().write(JSONSerializer.INSTANCE.toBytes(ack)) ;
  }
  
  private String getTopic(HttpServletRequest req) {
    String path  = req.getPathInfo() ;
    if(path == null) return "unknown-topic" ;
    return path.substring(1, path.length()) ;
  }
}
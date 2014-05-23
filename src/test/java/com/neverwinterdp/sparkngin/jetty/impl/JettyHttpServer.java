package com.neverwinterdp.sparkngin.jetty.impl;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class JettyHttpServer {
  Server server ;
  
  public JettyHttpServer(int port) {
    server = new Server(port);

    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.addServlet(new ServletHolder(new MessageServlet()), "/message/*");

    ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { context });

    server.setHandler(contexts);
  }
  
  public void start() throws Exception {
    server.start();
  }
  
  public void join() throws Exception {
    server.join() ;
  }
  
  public void stop() throws Exception {
    server.stop() ;
  }
  
  public static void main(String[] args) throws Exception {
    JettyHttpServer server = new JettyHttpServer(8080);
    server.start() ;
    System.out.println("Start.....................");
  }
}

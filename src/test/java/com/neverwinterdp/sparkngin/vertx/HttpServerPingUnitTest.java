package com.neverwinterdp.sparkngin.vertx;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.impl.DefaultVertxFactory;
import org.vertx.java.platform.Verticle;

import com.neverwinterdp.sparkngin.vertx.impl.EmbbededVertxServer;
import com.neverwinterdp.util.IOUtil;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class HttpServerPingUnitTest {
  final static int LISTEN_PORT = 8181 ;
  final static String PING_URL = "http://127.0.0.1:" + LISTEN_PORT + "/ping" ;
 
  private EmbbededVertxServer server ;
  
  @Before
  public void setup() throws Exception {
    server = new EmbbededVertxServer() ;
    server.deployVerticle(HttpServerPingVerticle.class, 1);
    Thread.sleep(2000);
  }
  
  @After
  public void teardown() throws Exception {
    server.stop(); 
  }
    
  @Test
  public void testVertxHttpClientGet() throws Exception {
    DefaultVertxFactory factory = new DefaultVertxFactory() ;
    Vertx vertx = factory.createVertx() ;
    HttpClient client = vertx.createHttpClient() ;
    client.setHost("127.0.0.1").setPort(LISTEN_PORT) ;
    client.get("/ping", new Handler<HttpClientResponse>() {
      public void handle(HttpClientResponse event) {
        event.bodyHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            String message = data.toString() ;
            Assert.assertEquals(HttpServerPingVerticle.REPLY_MESSAGE, message);
          }
        });
      }
    }).end() ;
    client.close();
    Thread.sleep(1000);
  }
  
  @Test
  public void testVertxHttpClientPost() throws Exception {
    DefaultVertxFactory factory = new DefaultVertxFactory() ;
    Vertx vertx = factory.createVertx() ;
    HttpClient client = vertx.createHttpClient() ;
    client.setHost("127.0.0.1").setPort(LISTEN_PORT) ;
    HttpClientRequest postReq = client.post("/ping", new Handler<HttpClientResponse>() {
      public void handle(HttpClientResponse event) {
        event.bodyHandler(new Handler<Buffer>() {
          public void handle(Buffer data) {
            String message = data.toString() ;
            Assert.assertEquals(HttpServerPingVerticle.REPLY_MESSAGE, message);
          }
        });
      }
    });
    postReq.setChunked(true) ;
    for(int i = 3; i < 1; i++) {
      postReq.write("this is a message to test " + i + "\n") ;
    }
    postReq.end();
    client.close();
    Thread.sleep(2000);
  }

  @Test
  public void testHttpClientGet() throws Exception {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create() ;
    org.apache.http.client.HttpClient httpClient = httpClientBuilder.build();
    HttpGet getRequest = new HttpGet(PING_URL) ;
    HttpResponse response = httpClient.execute(getRequest);
    String message = IOUtil.getStreamContentAsString(response.getEntity().getContent(), "UTF-8") ;
    Assert.assertEquals(HttpServerPingVerticle.REPLY_MESSAGE, message);
  }

  
  @Test
  public void testHttpClientPost() throws Exception {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create() ;
    org.apache.http.client.HttpClient httpClient = httpClientBuilder.build();

    for(int i = 0; i < 5; i++) {
      HttpPost postRequest = new HttpPost(PING_URL) ;
      StringEntity input = new StringEntity("{'test': 'this is a test'}");
      input.setContentType("application/json");
      postRequest.setEntity(input);
      HttpResponse response = httpClient.execute(postRequest);
      postRequest.abort()  ;
      String message = IOUtil.getStreamContentAsString(response.getEntity().getContent(), "UTF-8") ;
      Assert.assertEquals(HttpServerPingVerticle.REPLY_MESSAGE, message);
    }
  }
 
  static public class HttpServerPingVerticle extends Verticle {
    final static public String REPLY_MESSAGE = "pong!" ;
    
    public void start() {
      RouteMatcher matcher = new RouteMatcher();
      Handler<HttpServerRequest> getHandler = new Handler<HttpServerRequest>() {
        public void handle(final HttpServerRequest req) {
          req.response().putHeader("Content-Type", "text/plain");
          req.response().end(REPLY_MESSAGE);
        }
      };
      matcher.get("/ping", getHandler) ;
      
      Handler<HttpServerRequest> postHandler = new Handler<HttpServerRequest>() {
        public void handle(final HttpServerRequest req) {
          req.response().setStatusCode(200);
          req.response().putHeader("Content-Type", "application/json");
          req.response().setChunked(true) ;
          req.bodyHandler(new Handler<Buffer>() {
            public void handle(Buffer event) {
              byte[] bytes = event.getBytes() ;
              System.out.println("Received: " + new String(bytes));
              req.response().end(REPLY_MESSAGE);
            }
          });
        }
      };
      matcher.post("/ping", postHandler) ;
      
      final HttpServer server = getVertx().createHttpServer();
      server.requestHandler(matcher) ;
      server.listen(LISTEN_PORT);
      System.out.println("Server started");
    }
    
  }
}

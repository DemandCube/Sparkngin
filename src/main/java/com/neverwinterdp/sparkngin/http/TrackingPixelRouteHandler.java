package com.neverwinterdp.sparkngin.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import com.neverwinterdp.message.Message;
import com.neverwinterdp.netty.http.RouteHandlerGeneric;
import com.neverwinterdp.sparkngin.Sparkngin;
import com.neverwinterdp.util.JSONSerializer;
import com.neverwinterdp.util.MapUtil;
import com.neverwinterdp.util.text.StringUtil;
/**
 * Route to handle returning a 1x1 100% transparent png
 * Responds to GET requests in HTTP
 * @author Richard Duarte
 */
public class TrackingPixelRouteHandler extends RouteHandlerGeneric {
//This is a byte array for a 1x1 100% transparent .png image 
  final static ByteBuf IMAGE = Unpooled.wrappedBuffer(
                    new byte[]
                    {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 
                      0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52, 
                      0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                      0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4, 
                      (byte)0x89, 0x00, 0x00, 0x00, 0x06, 0x62, 0x4B, 0x47,
                      0x44, 0x00, (byte)0xFF, 0x00, (byte)0xFF, 0x00, (byte)0xFF, (byte)0xA0,
                      (byte)0xBD, (byte)0xA7, (byte)0x93, 0x00, 0x00, 0x00, 0x09, 0x70, 
                      0x48, 0x59, 0x73, 0x00, 0x00, 0x0B, 0x13, 0x00,
                      0x00, 0x0B, 0x13, 0x01, 0x00, (byte)0x9A, (byte)0x9C, 0x18,
                      0x00, 0x00, 0x00, 0x07, 0x74, 0x49, 0x4D, 0x45,
                      0x07, (byte)0xDE, 0x08, 0x14, 0x14, 0x24, 0x12, 0x12,
                      (byte)0x95, (byte)0xC7, (byte)0xB4, 0x00, 0x00, 0x00, 0x0C, 0x69,
                      0x54, 0x58, 0x74, 0x43, 0x6F, 0x6D, 0x6D, 0x65,
                      0x6E, 0x74, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xBC,
                      (byte)0xAE, (byte)0xB2, (byte)0x99, 0x00, 0x00, 0x00, 0x10, 0x49,
                      0x44, 0x41, 0x54, 0x08, 0x1D, 0x01, 0x05, 0x00,
                      (byte)0xFA, (byte)0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                      0x05, 0x00, 0x01, (byte)0xBA, (byte)0x89, 0x10, (byte)0x8A, 0x00,
                      0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE,
                      0x42, 0x60, (byte)0x82});
  
  
  private Sparkngin sparkngin ;
  private AtomicLong idTracker  = new AtomicLong();
  private Pattern[]  headerMatcher ;
  
  public TrackingPixelRouteHandler(Sparkngin sparkngin, Map<String, String> props) {
    this.sparkngin = sparkngin ;
    
    String extractHeaders = MapUtil.getString(props,"tracking.site.extract-headers", null) ;
    if(extractHeaders != null) {
      String[] extractHeader = StringUtil.toStringArray(extractHeaders) ;
      headerMatcher = new Pattern[extractHeader.length] ;
      for(int i = 0; i < extractHeader.length; i++) {
        headerMatcher[i] = Pattern.compile(extractHeader[i], Pattern.CASE_INSENSITIVE) ;
      }
    }
  }
  
  /**
   * Configure the handler and connection to sparkngin if sparkngin.connect is set
   */
  public void configure(Map<String, String> props) {
  }
  
  /**
   * Serves IMAGE as mimetype "image/png"
   * If we're supposed to forward these messages to sparkngin, then do so in a thread
   */
  protected void doGet(ChannelHandlerContext ctx, HttpRequest httpReq) {
    //Return Pixel to client
    FullHttpResponse response = this.createResponse(httpReq, IMAGE.retain(), "image/png") ;
    setCookie(httpReq, response) ;
    write(ctx, httpReq, response);
    sparknginLog(httpReq, response) ;
  }
  
  void sparknginLog(HttpRequest request, FullHttpResponse response) {
    //Send request log to sparkngin
    RequestLog log = new RequestLog(request, headerMatcher);
    Message message = new Message("id-" + idTracker.incrementAndGet(), log, false) ;
    message.getHeader().setTopic(log.getTrackerName()) ;
    sparkngin.push(message) ;
  }
  
  void setCookie(HttpRequest request, FullHttpResponse response) {
    // Encode the cookie.
    String cookieString = request.headers().get(COOKIE);
    if (cookieString != null) {
      Set<Cookie> cookies = CookieDecoder.decode(cookieString);
      if (!cookies.isEmpty()) {
        // Reset the cookies if necessary.
        for (Cookie cookie: cookies) {
          if("visit-count".equals(cookie.getName())) {
            int count = Integer.parseInt(cookie.getValue()) ;
            cookie.setValue(Integer.toString(count + 1));
            //System.out.println("Visit: " + count);
          }
          response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
        }
      }
    } else {
      // Browser sent no cookie.  Add some.
      response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("id", UUID.randomUUID().toString()));
      response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("visit-count", "1"));
      response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("first-visit-time", new Date().toString()));
    }
  }

  public static ByteBuf getImageBytes() { return IMAGE; }
}
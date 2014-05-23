package com.neverwinterdp.netty.http;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neverwinterdp.netty.http.route.RouteHandler;
import com.neverwinterdp.netty.http.route.RouteHandlerGeneric;
import com.neverwinterdp.netty.http.route.RouteMatcher;
/**
 * @author Tuan Nguyen
 * @email  tuan08@gmail.com
 */
public class RouteMatcherUnitTest {
  @Test
  public void testRouteMatcher() {
    RouteMatcher routeMatcher = new RouteMatcher() ;
    RouteHandler message = new RouteHandlerGeneric() ;
    RouteHandler topic = new RouteHandlerGeneric() ;
    routeMatcher.addPattern("/message", message);
    routeMatcher.addPattern("/message/:topic", topic);
    
    assertEquals(topic, routeMatcher.findHandler("/message/topic1")) ;
    assertEquals(topic, routeMatcher.findHandler("/message/topic2")) ;
    assertEquals(message,routeMatcher.findHandler("/message")) ;
  }
}

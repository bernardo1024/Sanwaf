package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class DatatypeModeTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-modes.xml");
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testParameter() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("modeParameter", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }

  @Test
  public void testParameterString() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("modeParameterString", "javascript: ");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.addParameter("modeParameterString", "javascript: <script> ");
    assertTrue(!sanwaf.isThreatDetected(request));
}

  @Test
  public void testParameterRegex() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("modeParameterRegex", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }


  @Test
  public void testHeader() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("modeHeader", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }

  @Test
  public void testCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("modeCookie", "foobarfoobar"));
    assertTrue(!sanwaf.isThreatDetected(request));
  }

  @Test
  public void testEndPoint() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterString", "javascript: ");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterRegex", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));

    
  }

  
}

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
  public void testParameterBlock() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameter-BLOCK", "foobarfoobar");
    assertTrue(sanwaf.isThreatDetected(request));
  }
  
  @Test
  public void testParameterDisabled() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameter-DISABLED", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }

  @Test
  public void testParameterItemRuleCombinations() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameter-DETECT-BLOCK", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameterString-DETECT-DISABLED", "javascript: <script> ");
    assertTrue(!sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameterRegex-BLOCK-BLOCK", "foobarfoobar");
    assertTrue(sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeParameterRegex-BLOCK-DISABLED", "foobarfoobar");
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
  public void testEndPointDetect() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter-DETECT", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterString-DETECT", "javascript: <script> ");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterRegex-DETECT", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter-DETECT", "foobarfoobar");
    request.addParameter("modeeParameterString", "javascript: <script> ");
    request.addParameter("modeeParameterRegex-DETECT", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }
  
  @Test
  public void testEndPointBlock() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter-BLOCK", "foobarfoobar");
    assertTrue(sanwaf.isThreatDetected(request));
  }
  
  @Test
  public void testEndPointDisabled() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter-DISABLED", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }

  @Test
  public void testEndPointItemRuleCombinations() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameter-DETECT-BLOCK", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterString-DETECT-DISABLED", "javascript: <script> ");
    assertTrue(!sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterRegex-BLOCK-BLOCK", "foobarfoobar");
    assertTrue(sanwaf.isThreatDetected(request));
    
    request = new MockHttpServletRequest();
    request.setRequestURI("/foo/bar/test.jsp");
    request.addParameter("modeeParameterRegex-BLOCK-DISABLED", "foobarfoobar");
    assertTrue(!sanwaf.isThreatDetected(request));
  }
    
}

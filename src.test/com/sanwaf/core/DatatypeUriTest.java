package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class DatatypeUriTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-uri.xml");
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testNumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestNumeric", "abcdefghij");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestNumeric", "abcdefghij");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestNumericDelimited", "abcdefghij");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestNumericDelimited", "abcdefghij");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestAlphanumeric", "!@#$%^&");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestAlphanumeric", "!@#$%^&");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumericAndMoreType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestAlphanumericAndMore", "!@#$%^&");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestAlphanumericAndMore", "!@#$%^&");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }


  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestChar", "abcdefghij");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestChar", "abcdefghij");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testRegexType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestRegex", "abcdefghij");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestRegex", "abcdefghij");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testConstantType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestConstant", "abcdefghij");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestConstant", "abcdefghij");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testJava() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestJava", "100");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestJava", "100");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testString() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestString", "<script>alert(1)</script>");
    assertEquals(true, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestString", "<script>alert(1)</script>");
    assertEquals(false, sanwaf.isThreatDetected(req));
  }

}

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
    req.addParameter("unitTestNumeric", "123");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestNumeric", "123");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestNumericDelimited", "123,456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestNumericDelimited", "123,456");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestAlphanumeric", "abc123");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestAlphanumeric", "abc123");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumericAndMoreType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestAlphanumericAndMore", "abc123 :");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestAlphanumericAndMore", "abc123 :");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestChar", "c");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestChar", "c");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testRegexType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestRegex", "555-555-5555");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestRegex", "555-555-5555");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testConstantType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestConstant", "FOO");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestConstant", "FOO");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testJava() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestJava", "10");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestJava", "10");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testString() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestString", "valid string");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestString", "valid string");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testMultipleUris() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("unitTestMultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/far/nar");
    req.addParameter("unitTestMultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/tar/mar");
    req.addParameter("unitTestMultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("unitTestMultipleUris", "123456");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }
}

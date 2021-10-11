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
    req.addParameter("Numeric", "123");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Numeric", "123");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("NumericDelimited", "123,456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("NumericDelimited", "123,456");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Alphanumeric", "abc123");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Alphanumeric", "abc123");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testAlphanumericAndMoreType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("AlphanumericAndMore", "abc123 :");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("AlphanumericAndMore", "abc123 :");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Char", "c");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Char", "c");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testRegexType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Regex", "555-555-5555");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Regex", "555-555-5555");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testConstantType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Constant", "FOO");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Constant", "FOO");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testJava() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Java", "10");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Java", "10");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testString() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("String", "valid string");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("String", "valid string");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testOpen() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("Open", "valid string");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("Open", "valid string");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }

  @Test
  public void testMultipleUris() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar");
    req.addParameter("MultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/far/nar");
    req.addParameter("MultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/tar/mar");
    req.addParameter("MultipleUris", "123456");
    assertEquals(false, sanwaf.isThreatDetected(req));

    req = new MockHttpServletRequest();
    req.setRequestURI("/foo/bar/invalid");
    req.addParameter("MultipleUris", "123456");
    assertEquals(true, sanwaf.isThreatDetected(req));
  }
}

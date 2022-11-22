package com.sanwaf.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class CustomPropertiesFooTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @Test
  public void fooXmlResourceTest() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-foo.xml");
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }

    MockHttpServletRequest req = new MockHttpServletRequest();

    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", "0123456789"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "foo.12"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "12.bar"));

    assertEquals(false, shield.threat(req, shield.parameters, "NumericDelimited", "-12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "NumericDelimited", "121,23"));
    assertEquals(true, shield.threat(req, shield.parameters, "NumericDelimited", "+foobar"));
    assertEquals(true, shield.threat(req, shield.parameters, "NumericDelimited", "123bar"));

    assertEquals(false, shield.threat(req, shield.parameters, "Alphanumeric", "abcdefghijklmnopqrstuvwxyz0123456789"));
    assertEquals(true, shield.threat(req, shield.parameters, "Alphanumeric", "1239.a"));

    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "abcde"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?234"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?a1b2c?3d4"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123-456"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123_456"));

    assertEquals(false, shield.threat(req, shield.parameters, "Char", "a"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "1"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "-"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", " "));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "12"));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "12345"));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "<asdffff."));

    assertEquals(false, shield.threat(req, shield.parameters, "CustomRegexSSN", "555-55-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "abc-de-fghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "555555555"));

    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "5555555555"));

    assertEquals(false, shield.threat(req, shield.parameters, "CustomTel", "555-555-5555"));
    assertEquals(false, shield.threat(req, shield.parameters, "CustomDate", "2016-01-01"));

    assertEquals(true, shield.threat(req, shield.parameters, "CustomDate", "2016xxdd"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomTel", "55-555-55556"));

    assertEquals(true, shield.threat(req, shield.parameters, "fooaFoo", "abcdefghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "foobFoo", "abcdefghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "foocFoo", "abcdefghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "foodFoo", "abcdefghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "fooeFoo", "abcdefghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "foofFoo", "abcdefghi"));

    assertEquals(false, shield.threat(req, shield.parameters, "fooaFoo", "12345,67890"));
    assertEquals(false, shield.threat(req, shield.parameters, "foobFoo", "12345,67890"));
    assertEquals(false, shield.threat(req, shield.parameters, "foocFoo", "12345,67890"));
    assertEquals(false, shield.threat(req, shield.parameters, "foodFoo", "12345,67890"));
    assertEquals(false, shield.threat(req, shield.parameters, "fooeFoo", "12345,67890"));
    assertEquals(false, shield.threat(req, shield.parameters, "foofFoo", "12345,67890"));

    assertEquals(false, shield.threat(req, shield.parameters, "*foo", "<script>alert(1)</script>"));
  }
}

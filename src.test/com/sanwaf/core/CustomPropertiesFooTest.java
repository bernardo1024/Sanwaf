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

    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", "0123456789", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "foo.12", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "12.bar", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "NumericDelimited", "-12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "NumericDelimited", "121,23", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "NumericDelimited", "+foobar", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "NumericDelimited", "123bar", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "Alphanumeric", "abcdefghijklmnopqrstuvwxyz0123456789", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "Alphanumeric", "1239.a", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "abcde", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?234", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?a1b2c?3d4", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123-456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123_456", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "Char", "a", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "-", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", " ", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "12", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "12345", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "<asdffff.", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "CustomRegexSSN", "555-55-5555", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "abc-de-fghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "555555555", false, false));

    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "5555555555", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "CustomTel", "555-555-5555", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "CustomDate", "2016-01-01", false, false));

    assertEquals(true, shield.threat(req, shield.parameters, "CustomDate", "2016xxdd", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomTel", "55-555-55556", false, false));

    assertEquals(true, shield.threat(req, shield.parameters, "fooaFoo", "abcdefghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "foobFoo", "abcdefghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "foocFoo", "abcdefghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "foodFoo", "abcdefghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "fooeFoo", "abcdefghi", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "foofFoo", "abcdefghi", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "fooaFoo", "12345,67890", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "foobFoo", "12345,67890", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "foocFoo", "12345,67890", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "foodFoo", "12345,67890", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "fooeFoo", "12345,67890", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "foofFoo", "12345,67890", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "*foo", "<script>alert(1)</script>", false, false));
  }
}


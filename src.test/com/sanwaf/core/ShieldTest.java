package com.sanwaf.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Error;
import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class ShieldTest {
  static Sanwaf sanwaf;
  static Shield shield;
  static String breakMaxSizeString = null;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf();
      shield = UnitTestUtil.getShield(sanwaf, "xss");

      String xssErrorString = "<script>alert(1)<script>";
      StringBuilder sb = new StringBuilder(xssErrorString);
      for (int i = 0; i < 5000; i++) {
        sb.append(xssErrorString);
      }
      breakMaxSizeString = sb.toString();
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testRegexMatch() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "String", "some text <script> some other script... </script>");
    for (Error error : errors) {
      System.out.println("String(XSS1)=" + error.toJson());
    }

    errors = sanwaf.getError(req, shield, "String", "javascript: abc123<script abc123> foo <script bar");
    for (Error error : errors) {
      System.out.println("String(XSS2)=" + error.toJson());
    }

    errors = sanwaf.getError(req, shield, "String",
        "any string<B>ANY  etc.) and here foo (bar). and we go until quotes . <font color=\" this should not be highlighted) and this (one, and should) be=\" not this");
    for (Error error : errors) {
      System.out.println("String(XSS3)=" + error.toJson());
    }

    req = new MockHttpServletRequest();
    errors = sanwaf.getError(req, shield, "String", "some text <script> some other script... <script>");
    for (Error error : errors) {
      System.out.println("String(XSS)=" + error.toJson());
    }

    errors = sanwaf.getError(req, shield, "Numeric", "123abc456");
    for (Error error : errors) {
      System.out.println("Number=" + error.toJson());
    }
}

  @Test
  public void TestToJson() {
    Item p1 = new ItemString("key1", 3, 2, "error msg1", null);
    Error error1 = new Error(shield, p1, "key1", "value2");
    Item p2 = new ItemNumeric("key11", 1000, 1, "error msg2", null);
    Error error2 = new Error(shield, p2, "key11", "value22");
    System.out.println(error1.toJson());
    System.out.println(Error.toJson(null));
    List<Error> errors = new ArrayList<Error>();
    errors.add(error1);
    errors.add(error2);
    System.out.println(Error.toJson(errors));
  }

  @Test
  public void testXssTooBig() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "String", breakMaxSizeString);
    assertEquals(false, b);
  }

  @Test
  public void testNullKeyValue() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, null, "<script>alert(1)</script>");
    assertEquals(false, b);
    b = shield.threat(req, shield.parameters, "String", null);
    assertEquals(false, b);
  }

  @Test
  public void testUnprotectedParameter() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "foobarNotInParmStore", "<script>alert(1)</script>");
    assertEquals(false, b);
  }

  @Test
  public void testThreatNoMetadata() {
    boolean b = shield.threat("<script>alert(1)</script>");
    assertEquals(true, b);
  }

  @Test
  public void testMetadataGetFromIndex() {
    String s = shield.parameters.getFromIndex("*foo");
    assertEquals(null, s);

    s = shield.parameters.getFromIndex("foo*");
    assertEquals(null, s);

    s = shield.parameters.getFromIndex("foo[*]");
    assertEquals(null, s);

  }

  @Test
  public void disableSanwafTest() {
    sanwaf.enabled = false;
    testNumeric(false);
    sanwaf.enabled = true;
  }

  @Test
  public void sanwafInvalidHttpRequestTest() {
    MockHttpServletRequest request = null;
    assertEquals(false, sanwaf.isThreatDetected(request));
    assertEquals(false, sanwaf.isThreatDetected(null));
  }

  @Test
  public void enabledTest() {
    shield.parameters.enabled = true;
    shield.headers.enabled = true;
    shield.cookies.enabled = true;
    testNumeric(true);
  }

  @Test
  public void disabledTest() {
    shield.parameters.enabled = false;
    shield.headers.enabled = false;
    shield.cookies.enabled = false;
    testNumeric(false);
  }

  void testNumeric(boolean isThreat) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("aParameterNumber", "foo.12");
    assertEquals(isThreat, sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.addHeader("aHeaderNumber", "foo.12");
    assertEquals(isThreat, sanwaf.isThreatDetected(request));

    request = new MockHttpServletRequest();
    request.setCookies(new Cookie("aCookieNumber", "foo.12"));
    assertEquals(isThreat, sanwaf.isThreatDetected(request));
  }
}

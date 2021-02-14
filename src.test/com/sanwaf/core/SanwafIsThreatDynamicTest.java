package com.sanwaf.core;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Sanwaf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SanwafIsThreatDynamicTest {
  static Sanwaf sanwaf;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-isThreat.xml");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testSanWafIsThreatString() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("<script>alert(1)</script>", "XSS", "<item><name>string</name><type>s</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"<script>alert(1)<\\/script>\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("valid string", "XSS", "<item><name>string</name><type>s</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("<script>alert(1)</script>", "XSS", "<item><name>string</name><type>s</type><max></max><min></min><msg></msg><uri></uri></item>", false, request);
    assertTrue(result == true);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }
  
  @Test
  public void testSanWafIsThreatNumeric() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123", "XSS", "<item><name>numeric</name><type>n</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("-123.456", "XSS", "<item><name>numeric</name><type>n</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatNumericDelimited() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123", "XSS", "<item><name>numericDelimited</name><type>n{,}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("-123.456,789", "XSS", "<item><name>numericDelimited</name><type>n{,}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatAlphanumeric() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123!!!", "XSS", "<item><name>alphanumeric</name><type>a</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("abc123", "XSS", "<item><name>alphanumeric</name><type>a</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatAlphanumericAndMore() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123!!!", "XSS", "<item><name>alphanumericAndMore</name><type>a{?\\s:}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("abc  123", "XSS", "<item><name>alphanumericAndMore</name><type>a{?\\s:}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatChar() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123!!!", "XSS", "<item><name>char</name><type>c</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("a", "XSS", "<item><name>char</name><type>c</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatRegex() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123!!!", "XSS", "<item><name>regex</name><type>r{telephone}</type><max>12</max><min>12</min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("555-555-5555", "XSS", "<item><name>regex</name><type>r{telephone}</type><max>12</max><min>12</min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatConstant() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("abc123!!!", "XSS", "<item><name>constant</name><type>k{FOO,BAR,FAR}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("FOO", "XSS", "<item><name>constant</name><type>k{FOO,BAR,FAR}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatJava() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("100", "XSS", "<item><name>java</name><type>j{com.sanwaf.core.JavaClass.over10TrueElseFalse()}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"") >= 0);

    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("9", "XSS", "<item><name>java</name><type>j{com.sanwaf.core.JavaClass.over10TrueElseFalse()}</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatMaxMinMsgUri() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/foobar");
    boolean result = sanwaf.isThreat("12345", "XSS", "<item><name>MaxMinMsgUri</name><type>n</type><max>5</max><min>5</min><msg>max(5)min(5)uri(</msg><uri>/foobar</uri></item>", true, request);
    assertTrue(result == false);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s == null);

    request = new MockHttpServletRequest();
    request.setRequestURI("/foobar");
    result = sanwaf.isThreat("1", "XSS", "<item><name>MaxMinMsgUri</name><type>n</type><max>5</max><min>5</min><msg>max(5)min(5)uri(</msg><uri>/foobar</uri></item>", true, request);
    assertTrue(result == true);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    s = Sanwaf.getErrors(request);
    assertTrue(s != null);

    request = new MockHttpServletRequest();
    request.setRequestURI("/foobar");
    result = sanwaf.isThreat("123456", "XSS", "<item><name>MaxMinMsgUri</name><type>n</type><max>5</max><min>5</min><msg>max(5)min(5)uri(</msg><uri>/foobar</uri></item>", true, request);
    assertTrue(result == true);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    s = Sanwaf.getErrors(request);
    assertTrue(s != null);

    request = new MockHttpServletRequest();
    request.setRequestURI("/badUri");
    result = sanwaf.isThreat("123456", "XSS", "<item><name>MaxMinMsgUri</name><type>n</type><max>5</max><min>5</min><msg>max(5)min(5)uri(</msg><uri>/foobar</uri></item>", true, request);
    assertTrue(result == true);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    s = Sanwaf.getErrors(request);
    assertTrue(s != null);

  
  }

  @Test
  public void testSanWafIsThreatDynamicXmlInvalidShieldName() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    boolean result = sanwaf.isThreat("<script>alert(1)</script>", "INVALID", "<item><name>string</name><type>s</type><max></max><min></min><msg></msg><uri></uri></item>", true, request);
    assertTrue(result == false);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }  
}

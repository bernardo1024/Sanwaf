package com.sanwaf.core;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SanwafIsThreatTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-isThreat.xml");
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testSanWafIsThreat() { // test a non-mapped parm allows all
    boolean b = sanwaf.isThreat("<script>alert(1)</script>");
    assertEquals(true, b);

    b = sanwaf.isThreat("alert(1)");
    assertEquals(false, b);
  }

  @Test
  public void testThreatWithNull() {
    boolean b = sanwaf.isThreat(null);
    assertTrue(!b);
  }

  @Test
  public void testSanWafIsThreatSetAttributesParameters() { // test a non-mapped parm allows all
    MockHttpServletRequest request = new MockHttpServletRequest();
    request = new MockHttpServletRequest();

    boolean result = sanwaf.isThreat("<script>alert(1)</script>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s.indexOf("{\"key\":\"null\",\"value\":\"<script>alert(1)<\\/script>\"") >= 0);
    
    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("< script>alert(1)</ script>", true, request);
    assertTrue(result == false);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
    
    request = new MockHttpServletRequest();
    result = sanwaf.isThreat("<script>alert(1)</script>", false, request);
    assertEquals(true, result);
    trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId == null);
    s = Sanwaf.getErrors(request);
    assertTrue(s == null);
  }

  @Test
  public void testSanWafIsThreatDoNotAddErrorParms() { // test a non-mapped parm allows all
    MockHttpServletRequest request = new MockHttpServletRequest();
    request = new MockHttpServletRequest();
    boolean orig = sanwaf.onErrorAddParmErrors;
    sanwaf.onErrorAddParmErrors = false;
    boolean result = sanwaf.isThreat("<script>alert(1)</script>", true, request);
    assertTrue(result == true);
    String trackId = Sanwaf.getTrackingId(request);
    assertTrue(trackId != null);
    String s = Sanwaf.getErrors(request);
    assertTrue(s == null);
    sanwaf.onErrorAddParmErrors = orig;
  }
}

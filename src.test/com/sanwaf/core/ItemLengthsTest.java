package com.sanwaf.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class ItemLengthsTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf();
      shield = UnitTestUtil.getShield(sanwaf, "XSS");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testNumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthN_0_5", "12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthN_0_5", "12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthN_0_5", "-1234", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthN_0_5", "123456", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthN2_0_5", "12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthN2_0_5", "-1234", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthN2_0_5", "123456", false, false));
  }

  @Test
  public void testNumericDelimited() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN_6_6", "123456", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN_6_6", "-12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN_6_6", "123456,123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN_6_6", "+123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN_6_6", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN_6_6", "1234", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN2_6_6", "123456", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN2_6_6", "-12345", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN2_6_6", "123456,123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN2_6_6", "+123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN2_6_6", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN2_6_6", "1234", false, false));
  }

  @Test
  public void testAlphanumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthA_0_3", "ab1", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthA_0_3", "abc4", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthA2_0_3", "ab1", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthA2_0_3", "abc4", false, false));
  }

  @Test
  public void testAlphanumericAndMore() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA_0_4", "abc1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA_0_4", "ab:1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA_0_4", "a:2:", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthAA_0_4", "123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthAA_0_4", "12:346", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA2_0_4", "abc1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA2_0_4", "ab:1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthAA2_0_4", "a:2:", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthAA2_0_4", "123456", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthAA2_0_4", "12:346", false, false));
  }

  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC_1_1", "a", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC_1_1", "1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC_1_1", "-", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC_1_1", " ", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC_1_1", "12", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC_1_1", "12345", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC_1_1", "<asdffff.", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthC2_1_1", "a", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC2_1_1", "1", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC2_1_1", "-", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthC2_1_1", " ", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC2_1_1", "12", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC2_1_1", "12345", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthC2_1_1", "<asdffff.", false, false));
  }

  @Test
  public void testCustomRegex() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "lengthR_0_11", "555-55-5555", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR_0_11", "abc-de-fghi1", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthR2_0_11", "555-55-5555", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR2_0_11", "abc-de-fghi1", false, false));
  }

  @Test
  public void testStringType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(true, shield.threat(req, shield.parameters, "lengthS2_0_7", "12345678", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthS_0_7", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthS_0_7", "12345678", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthS2_0_7", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthS2_0_7", "12345678", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthS_0_7", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthS_0_7", "12345678", false, false));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthS2_0_7", "1234567", false, false));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthS2_0_7", "12345678", false, false));
  }

  @Test
  public void testStringTypeMinSetNoValue() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(true, shield.threat(req, shield.parameters, "lengthNN2_6_6", "", false, false));
    assertEquals(false, shield.threat(req, shield.parameters, "lengthNN2_6_6", null, false, false));
  }

  @Test
  public void TestMaxMinLength() {
    Shield shield = UnitTestUtil.getShield(sanwaf, "ParmLength");
    assertTrue(shield.maxLen == Integer.MAX_VALUE);
    assertTrue(shield.minLen == 0);
    assertTrue(shield.regexMinLen == Integer.MAX_VALUE);

    Item p = shield.getItem(shield.parameters, "MaxMinLen");
    assertTrue(p.max == Integer.MAX_VALUE);
    assertTrue(p.min == 0);
  }
}


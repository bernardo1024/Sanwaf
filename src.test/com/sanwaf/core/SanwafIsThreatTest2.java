package com.sanwaf.core;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Sanwaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SanwafIsThreatTest2 {
  static Sanwaf sanwaf;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-isThreat2.xml");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testRegex() {
    assertFalse(sanwaf.isThreat("foobar"));
    assertTrue(sanwaf.isThreat("foo1bar"));
  }

}


package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class AlwaysPerformRegexTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-verboseRegexAlways.xml");
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testRegexAlways() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("unitTestStringExcluded", "<script>alert(1)</script>");
    Boolean result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(false));

    request = new MockHttpServletRequest();
    request.addParameter("foobar", "<script>alert(1)</script>");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));
  }
}

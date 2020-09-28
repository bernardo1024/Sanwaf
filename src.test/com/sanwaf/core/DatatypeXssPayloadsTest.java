package com.sanwaf.core;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class DatatypeXssPayloadsTest {
  static Sanwaf sanwaf;
  static Shield shield;

  static int iterations = 1;
  static boolean doHex = true;
  static boolean logErrors = true;

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
  public void XssFormElements() {
    UnitTestResult result = UnitTestUtil.runTestsUsingFile(shield, "src.test/resources/xssFormPayloads.txt", iterations, doHex, logErrors);
    UnitTestUtil.log("XSS-SanWaf", result);
  }

}

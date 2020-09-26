package com.sanwaf.core;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sanwaf.log.LoggerSystemOut;

public class OtherClassesTest {
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
  public void TestDefaultContructorParameterItem() {
    Parameter pi = new ParameterString();
    String s = pi.toString();
    assertTrue(s.contains(", max: " + Integer.MAX_VALUE + ", min: 0"));
  }

  @Test
  public void TestLoggerSystemOut() {
    LoggerSystemOut logger = new LoggerSystemOut();
    logger.error("foobar-error");
    logger.info("foobar-info");

    assertTrue(true);
  }

  @Test
  public void TestErrorAddPoint() {
    Parameter p = new ParameterString("", 100, 0, "error msg", null);
    Error error = new Error(shield, p, "foo", "bar");
    Point point = new Point(0, 5);
    error.addPoint(point);
    String s = error.toJson();
    assertTrue(s.contains("foo"));
  }

  @Test
  public void TestErrorAddPointErrorMax() {
    Parameter p = new ParameterString("", 5, 0, "error msg", null);
    Error error = new Error(shield, p, "foo", "123456789");
    String s = error.toJson();
    assertTrue(s.contains("Invalid length"));
  }

  @Test
  public void TestErrorAddPointErrorMin() {
    Parameter p = new ParameterString("", 5, 5, "error msg", null);
    Error error = new Error(shield, p, "foo", "123");
    String s = error.toJson();
    assertTrue(s.contains("Invalid length"));
  }
}

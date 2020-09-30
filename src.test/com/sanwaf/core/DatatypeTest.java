package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class DatatypeTest {
  static Sanwaf sanwaf;
  static Shield shield;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf();
      shield = UnitTestUtil.getShield(sanwaf, "xss");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void testNumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();

    assertEquals(false, shield.threat(req, shield.parameters, "lengthN_0_5", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestNumeric", "0123456789"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestNumeric", "foo.12"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestNumeric", "12.bar"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestNumeric", "12.34.56.78"));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemNumericDelimited p = new ItemNumericDelimited("", "n{}", Integer.MAX_VALUE, 0, "", "");
    assertEquals(true, p.inError(req, shield, "12,34,56"));

    List<Point> list = p.getErrorPoints(shield, "");
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
  }

  @Test
  public void testAlphanumeric() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumeric", "abcdefghijklmnopqrstuvwxyz0123456789"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestAlphanumeric", "1239.a"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestAlphanumeric", "1239.a...."));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestAlphanumeric", "1239.abc"));
  }

  @Test
  public void testAlphanumericType2() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestAlphanumeric", "abcdefg123-22");
    for (Error error : errors) {
      System.out.println("unitTestAlphanumeric=" + error.toJson());
    }
    errors = sanwaf.getError(req, shield, "unitTestAlphanumeric", "a,b.c?defg123-22");
    for (Error error : errors) {
      System.out.println("unitTestAlphanumeric=" + error.toJson());
    }

  }

  @Test
  public void testAlphanumericAndMore() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", "abcde"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", "1?234"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", "1?a1b2c?3d4"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", "123-456"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", "123_456"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMore", null));
  }

  @Test
  public void testAlphanumericAndMoreType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemAlphanumericAndMore p = new ItemAlphanumericAndMore("", "a{,}", Integer.MAX_VALUE, 0, "", "");
    assertEquals(false, p.inError(req, shield, ""));
    assertEquals(false, p.inError(req, shield, null));
    assertEquals(false, p.inError(req, shield, "abcde"));
    assertEquals(true, p.inError(req, shield, "abcde?fg"));

    List<Point> list = p.getErrorPoints(shield, "abcde?fg");
    assertEquals(true, list.size() == 1);
    list = p.getErrorPoints(shield, "");
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, "1239.xyz");
    assertEquals(true, list.size() == 1);
  }

  @Test
  public void testAlphanumericAndMoreType2() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestAlphanumericAndMore", "abcde ***");
    for (Error error : errors) {
      System.out.println("unitTestAlphanumericAndMore=" + error.toJson());
    }
    errors = sanwaf.getError(req, shield, "unitTestAlphanumericAndMore", "abc123?;: ***");
    for (Error error : errors) {
      System.out.println("unitTestAlphanumericAndMore=" + error.toJson());
    }
  }

  @Test
  public void testAlphanumericAndMoreTypeSpecialChars() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMoreSpecialChars", "a b\tc\nd\re"));
  }

  @Test
  public void testAlphanumericAndMoreTypeCurlyBraces() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestAlphanumericAndMoreCurlyBraces", "{a}"));
  }

  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", "a"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", "1"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", "-"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", " "));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", "12"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestChar", "123456"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestChar", "<asdffff."));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestChar", null));

    ItemChar p = new ItemChar("", 1, 0, "", "");
    assertTrue(p.inError(req, shield, "12345"));
    assertFalse(p.inError(req, shield, "1"));
    assertFalse(p.inError(req, shield, ""));
    assertFalse(p.inError(req, shield, null));

  }

  @Test
  public void testCharType2() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestChar", "ab");
    for (Error error : errors) {
      System.out.println("unitTestChar=" + error.toJson());
    }
  }

  @Test
  public void testRegex() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestCustomRegexSSN", "555-55-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestCustomRegexSSN", "abc-de-fghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestCustomRegexSSN", "5555555555"));

    assertEquals(false, shield.threat(req, shield.parameters, "unitTestCustomTel", "555-555-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestCustomTel", "55-555-55556"));

    assertEquals(true, shield.threat(req, shield.parameters, "unitTestCustomDate", "20160101"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestCustomDate", "2016-01-01"));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthR_0_11", "555-55-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR_0_11", "abc-de-fghi"));

    assertEquals(false, shield.threat(req, shield.parameters, "lengthR2_0_11", "555-55-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR2_0_11", "abc-de-fghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR2_0_11", "555-55-55"));
    assertEquals(true, shield.threat(req, shield.parameters, "lengthR2_0_11", "555-55-5555-55"));
  }

  @Test
  public void testRegexType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemRegex p = new ItemRegex("", "r{telephone}", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.patternName != null && p.patternName.length() > 0);

    assertEquals(false, p.inError(req, shield, "416-555-5555"));
    assertEquals(true, p.inError(req, shield, "abc-def-ghij"));
    assertEquals(true, p.inError(req, shield, "a"));
    assertEquals(true, p.inError(req, shield, "abc-def-ghij-klmn"));

    List<Point> list = p.getErrorPoints(shield, "");
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, "416-555-5555");
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, "abc123def456");
    assertEquals(true, list.size() == 1);
  }

  @Test
  public void testRegexTypeInvalidFormta() {
    ItemRegex p = new ItemRegex("", "r telephone", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.patternName == null);
    assertTrue(p.pattern == null);
  }

  @Test
  public void testConstantType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestConstant", "FOO"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestConstant", "BAR"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestConstant", "FAR"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestConstant", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestConstant", null));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestConstant", "foo"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestConstant", "bar"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestConstant", "far"));

    ItemConstant p = new ItemConstant("", "k FOO,BAR", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.constants == null);
    p = new ItemConstant("", "k FOO}", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.constants == null);

    p = new ItemConstant("", "", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.constants == null);
  }

  @Test
  public void testPoint() {
    Point p = new Point(1, 100);
    assertTrue(p.toString().contains("start: 1, end: 100"));
  }

  @Test
  public void testJava() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestJava", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestJava", "10"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestJava", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestJava", null));

    assertEquals(false, shield.threat(req, shield.parameters, "unitTestJava", "0001"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestJava", "0000"));
  }

  @Test
  public void parseMethodNameTest() {
    assert (ItemJava.parseMethod("foo.method()").equals("method"));
    assert (ItemJava.parseMethod("foomethod()").equals("foomethod()"));
  }

  @Test
  public void testJavaMultipleParms() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("unitTestJavaMultiParm", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm2", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm3", "foobarfoobar");
    Boolean result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(false));

    request = new MockHttpServletRequest();
    request.addParameter("unitTestJavaMultiParm", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm2", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm3", "foobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));

    request = new MockHttpServletRequest();
    request.addParameter("unitTestJavaMultiParm", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm2", "foobar");
    request.addParameter("unitTestJavaMultiParm3", "foobarfoobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));

    request = new MockHttpServletRequest();
    request.addParameter("unitTestJavaMultiParm", "foobar");
    request.addParameter("unitTestJavaMultiParm2", "foobarfoobar");
    request.addParameter("unitTestJavaMultiParm3", "foobarfoobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));
  }

  @Test
  public void testJavaInvalidClass() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "unitTestJavaInvalidClass", "0000");
    assertEquals(true, b);

    req = new MockHttpServletRequest();
    b = shield.threat(req, shield.parameters, "unitTestJavaInvalidClassEmpty", "0000");
    assertEquals(true, b);

    req = new MockHttpServletRequest();
    b = shield.threat(req, shield.parameters, "unitTestJavaInvalidClassNoPackage", "0000");
    assertEquals(true, b);
  }

  @Test
  public void testJavaInvalidMethod() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "unitTestJavaInvalidMethod", "0000");
    assertEquals(true, b);
  }
}

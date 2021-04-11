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
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", "0123456789"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", "-12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", "-12345.67"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "foo.12"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "12.bar"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "12.34.56.78"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric", "- 12345.67"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "10"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "2"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "5"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "11"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "1"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "abc"));
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
    assertEquals(false, shield.threat(req, shield.parameters, "Alphanumeric", "abcdefghijklmnopqrstuvwxyz0123456789"));
    assertEquals(true, shield.threat(req, shield.parameters, "Alphanumeric", "1239.a"));
    assertEquals(true, shield.threat(req, shield.parameters, "Alphanumeric", "1239.a...."));
    assertEquals(true, shield.threat(req, shield.parameters, "Alphanumeric", "1239.abc"));
  }

  @Test
  public void testAlphanumericSizeError() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericSizeError", "123"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericSizeError", "1234"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericSizeError", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericSizeError", "123456"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericSizeError", "1234567"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericSizeError", "12345678"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericSizeError", "123456789"));
  }

  @Test
  public void testAlphanumericType2() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Alphanumeric", "abcdefg123-22");
    for (Error error : errors) {
      System.out.println("Alphanumeric=" + error.toJson());
    }
    errors = sanwaf.getError(req, shield, "Alphanumeric", "a,b.c?defg123-22");
    for (Error error : errors) {
      System.out.println("Alphanumeric=" + error.toJson());
    }

  }

  @Test
  public void testAlphanumericAndMore() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "abcde"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?234"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", "1?a1b2c?3d4"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123-456"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMore", "123_456"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMore", null));
  }

  @Test
  public void testAlphanumericAndMoreInvalidConfig() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreInvalidConfig1", "abc123? :"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreInvalidConfig2", "1?234"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreInvalidConfig3", "1?a1b2c?3d4"));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreInvalidConfig4", "abc123"));
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
    List<Error> errors = sanwaf.getError(req, shield, "AlphanumericAndMore", "abcde ***");
    for (Error error : errors) {
      System.out.println("AlphanumericAndMore=" + error.toJson());
    }
    errors = sanwaf.getError(req, shield, "AlphanumericAndMore", "abc123?;: ***");
    for (Error error : errors) {
      System.out.println("AlphanumericAndMore=" + error.toJson());
    }
  }

  @Test
  public void testAlphanumericAndMoreTypeSpecialChars() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreSpecialChars", "a b\tc\nd\re"));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMoreSpecialChars", "a \\"));
  }

  @Test
  public void testAlphanumericAndMoreTypeCurlyBraces() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreCurlyBraces", "{a}"));
  }

  @Test
  public void testChar() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "a"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "1"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", "-"));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", " "));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "12"));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "123456"));
    assertEquals(true, shield.threat(req, shield.parameters, "Char", "<asdffff."));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "Char", null));

    ItemChar p = new ItemChar("", 1, 0, "", "");
    assertTrue(p.inError(req, shield, "12345"));
    assertFalse(p.inError(req, shield, "1"));
    assertFalse(p.inError(req, shield, ""));
    assertFalse(p.inError(req, shield, null));
  }

  @Test
  public void testCharType2() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Char", "ab");
    for (Error error : errors) {
      System.out.println("Char=" + error.toJson());
    }
  }

  @Test
  public void testRegex() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "CustomRegexSSN", "555-55-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "abc-de-fghi"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomRegexSSN", "5555555555"));

    assertEquals(false, shield.threat(req, shield.parameters, "CustomTel", "555-555-5555"));
    assertEquals(true, shield.threat(req, shield.parameters, "CustomTel", "55-555-55556"));

    assertEquals(true, shield.threat(req, shield.parameters, "CustomDate", "20160101"));
    assertEquals(false, shield.threat(req, shield.parameters, "CustomDate", "2016-01-01"));

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
    assertEquals(false, shield.threat(req, shield.parameters, "Constant", "FOO"));
    assertEquals(false, shield.threat(req, shield.parameters, "Constant", "BAR"));
    assertEquals(false, shield.threat(req, shield.parameters, "Constant", "FAR"));
    assertEquals(false, shield.threat(req, shield.parameters, "Constant", null));
    assertEquals(true, shield.threat(req, shield.parameters, "Constant", ""));
    assertEquals(true, shield.threat(req, shield.parameters, "Constant", "foo"));
    assertEquals(true, shield.threat(req, shield.parameters, "Constant", "bar"));
    assertEquals(true, shield.threat(req, shield.parameters, "Constant", "far"));
    assertEquals(true, shield.threat(req, shield.parameters, "Constant", "FOOO"));

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
    assertEquals(true, shield.threat(req, shield.parameters, "Java", "12345"));
    assertEquals(true, shield.threat(req, shield.parameters, "Java", "12345678901"));//violates max setting
    assertEquals(true, shield.threat(req, shield.parameters, "Java", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "Java", "10"));
    assertEquals(false, shield.threat(req, shield.parameters, "Java", null));
    assertEquals(false, shield.threat(req, shield.parameters, "Java", "0001"));
    assertEquals(false, shield.threat(req, shield.parameters, "Java", "0000"));
  }

  @Test
  public void parseMethodNameTest() {
    assert (ItemJava.parseMethod("foo.method()").equals("method"));
    assert (ItemJava.parseMethod("foomethod()").equals("foomethod()"));
  }

  @Test
  public void testJavaMultipleParms() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addParameter("JavaMultiParm", "foobarfoobar");
    request.addParameter("JavaMultiParm2", "foobarfoobar");
    request.addParameter("JavaMultiParm3", "foobarfoobar");
    Boolean result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(false));

    request = new MockHttpServletRequest();
    request.addParameter("JavaMultiParm", "foobarfoobar");
    request.addParameter("JavaMultiParm2", "foobarfoobar");
    request.addParameter("JavaMultiParm3", "foobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));

    request = new MockHttpServletRequest();
    request.addParameter("JavaMultiParm", "foobarfoobar");
    request.addParameter("JavaMultiParm2", "foobar");
    request.addParameter("JavaMultiParm3", "foobarfoobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));

    request = new MockHttpServletRequest();
    request.addParameter("JavaMultiParm", "foobar");
    request.addParameter("JavaMultiParm2", "foobarfoobar");
    request.addParameter("JavaMultiParm3", "foobarfoobar");
    result = sanwaf.isThreatDetected(request);
    assertTrue(result.equals(true));
  }

  @Test
  public void testJavaInvalidClass() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "JavaInvalidClass", "0000");
    assertEquals(true, b);

    req = new MockHttpServletRequest();
    b = shield.threat(req, shield.parameters, "JavaInvalidClass2", "0000");
    assertEquals(true, b);

    req = new MockHttpServletRequest();
    b = shield.threat(req, shield.parameters, "JavaInvalidClassEmpty", "0000");
    assertEquals(true, b);

    req = new MockHttpServletRequest();
    b = shield.threat(req, shield.parameters, "JavaInvalidClassNoPackage", "0000");
    assertEquals(true, b);
  }

  @Test
  public void testJavaInvalidMethod() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "JavaInvalidMethod", "0000");
    assertEquals(false, b);
  }

  @Test
  public void testFormatType() {
    ItemFormat p = new ItemFormat("", "f{(###) ###-####", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.formatString != null);
    assertTrue(p.formatString.length() > 0);
  }

  @Test
  public void testInvalidFormatType() {
    ItemFormat p = new ItemFormat("", "f {(###) ###-####", Integer.MAX_VALUE, 0, "", "");
    assertTrue(p.formatString == null);
  }

  @Test
  public void testFormat() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat", "(123) 456-7890 abc ABC"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat", "BAR"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat", null));
  }

  @Test
  public void testFormatRequired() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmFormatRequired", "(123) 456-7890 abc ABC"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired", "BAR"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "parmFormatRequired", null));
  }


}

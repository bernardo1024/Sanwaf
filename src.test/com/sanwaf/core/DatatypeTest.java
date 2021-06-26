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
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", null));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "NumericRequired", null));
    assertEquals(true, shield.threat(req, shield.parameters, "NumericRequired", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "10"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "2"));
    assertEquals(false, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "5"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "11"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "1"));
    assertEquals(true, shield.threat(req, shield.parameters, "Numeric-maxval10-minval2", "abc"));
  }

  @Test
  public void testInteger() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "Integer", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "Integer", "0123456789"));
    assertEquals(false, shield.threat(req, shield.parameters, "Integer", "-12345"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", "-12345.67"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", "foo.12"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", "12.bar"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", "12.34.56.78"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", "- 12345"));
    assertEquals(true, shield.threat(req, shield.parameters, "Integer", " 12345"));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemNumericDelimited p = new ItemNumericDelimited("", "n{}", Integer.MAX_VALUE, 0, "", "", false);
    assertEquals(true, p.inError(req, shield, "12,34,56"));

    List<Point> list = p.getErrorPoints(shield, "");
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
    list = p.getErrorPoints(shield, null);
    assertEquals(true, list.size() == 0);
  }

  @Test
  public void testIntegerDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemNumericDelimited p = new ItemNumericDelimited("", "i{}", Integer.MAX_VALUE, 0, "", "", false);
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
    assertEquals(false, shield.threat(req, shield.parameters, "Alphanumeric", null));
    assertEquals(false, shield.threat(req, shield.parameters, "Alphanumeric", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericRequired", null));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericRequired", ""));
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
    assertEquals(false, shield.threat(req, shield.parameters, "AlphanumericAndMoreRequired", null));
    assertEquals(true, shield.threat(req, shield.parameters, "AlphanumericAndMoreRequired", ""));
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
    assertEquals(true, shield.threat(req, shield.parameters, "CharRequired", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "CharRequired", null));

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

    assertEquals(false, shield.threat(req, shield.parameters, "Regex", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "Regex", null));
    assertEquals(true, shield.threat(req, shield.parameters, "RegexRequired", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "RegexRequired", null));
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
    assertEquals(false, shield.threat(req, shield.parameters, "Constant", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "ConstantRequired", null));
    assertEquals(true, shield.threat(req, shield.parameters, "ConstantRequired", ""));
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
    assertEquals(false, shield.threat(req, shield.parameters, "Java", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "JavaRequired", null));
    assertEquals(true, shield.threat(req, shield.parameters, "JavaRequired", ""));
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

  @Test
  public void testFormat2Required() {
    //<item><name>parmFormatRequired2</name><type>f{\#\A\a\c #Aac}
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0ZzZ"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Zzz"));
    
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", " Aac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "{Aac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "-Aac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "# ac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#{ac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#-ac 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#A c 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#A{c 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#A-c 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aa  0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aa{ 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aa- 0Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac zZzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac {Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac -Zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0zzz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0{zz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0-zz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 00zz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Z0z"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0ZZz"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Z{z"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Z-z"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Zz "));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Zz0"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Zz{"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "#Aac 0Zz-"));

    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", "BAR"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmFormatRequired2", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "parmFormatRequired2", null));
  }
  
  @Test
  public void testFormat2() {
    //<item><name>parmformat2</name><type>f{#[1-12] / #[21-35]}</type>
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", "12 / 30"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", "01 / 30"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", "12 / 21"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", "12 / 35"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", "11 / 29"));
    
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "a0 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "z0 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "/0 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", ":0 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "00 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "13 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "93 / 25"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "01 / 20"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "01 / 36"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2", "01 / 99"));
    
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", ""));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2", null));
  }

  @Test
  public void testFormat2brackets() {
    //<item><name>parmformat2brackets</name><type>f{\[\]#[1-10]}</type>
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]01"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]02"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]03"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]04"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]05"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]06"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]07"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]08"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]09"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat2brackets", "[]10"));
    
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2brackets", "[]00"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2brackets", "[]99"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2brackets", " ]01"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat2brackets", "[ 01"));

    //<item><name>parmformat3</name><type>f{#[1-9]}</type>
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "1"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "2"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "3"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "4"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "5"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "6"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "7"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "8"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmformat3", "9"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat3", "10"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmformat3", "0"));
    
  }
  
  @Test
  public void testBadFormats() {
    //<item><name>parmformat2brackets</name><type>f{\[\]#[1-10]}</type>
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmbadformat1", "@"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmbadformat2", "@"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmbadformat3", "@"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmbadformat4", "@"));
  }

  
  @Test
  public void testMultiFormats() {
    //<item><name>parmMultiFormat1</name><type>f{#####||#####-####}</type></item>
    //<item><name>parmMultiFormat2</name><type>f{#####||#####-####||A#A-#A#}</type></item>
    //<item><name>parmMultiFormat3</name><type>f{#####||#####-####||A#A-#A#||A## A###}</type></item>
    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat1", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat1", "12345-6789"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmMultiFormat1", "@"));
    
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat2", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat2", "12345-6789"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat2", "A1B-2C3"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmMultiFormat2", "@"));
    
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat3", "12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat3", "12345-6789"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat3", "A1B-2C3"));
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormat3", "A12 B345"));
    assertEquals(true, shield.threat(req, shield.parameters, "parmMultiFormat3", "A12 B5"));
    
    assertEquals(false, shield.threat(req, shield.parameters, "parmMultiFormatInvalid", "A12 B5"));
  }

  
  
  
}

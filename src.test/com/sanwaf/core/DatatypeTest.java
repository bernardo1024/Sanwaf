package com.sanwaf.core;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Shield;
import com.sanwaf.core.Datatype;
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
  public void testNumericType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    Datatype dt = Datatype.NUMERIC;
    assertEquals(false, dt.inError(req, shield, "lengthN_0_5", "", 0));
    assertEquals(false, dt.inError(req, shield, "lengthN_0_5", null, 0));
  }

  @Test
  public void testNumericType2() {
    Datatype.defaultErrorMessages.put(Datatype.NUMERIC, "default num err msg");
    List<Point> temp = Datatype.NUMERIC.getErrorHighlightPoints(shield, "unitTestNumber", "1a2b3c4d5efg");
    System.out.println("unitTestNumber=" + temp);
    temp = Datatype.NUMERIC.getErrorHighlightPoints(shield, "unitTestNumber", "-1a2b3c4d5efg");
    System.out.println("unitTestNumber=" + temp);
    temp = Datatype.NUMERIC.getErrorHighlightPoints(shield, "unitTestNumber", "-1a2b3c4d5efg.01.02");
    System.out.println("unitTestNumber=" + temp);

  }

  @Test
  public void testNumericDelimitedType2() {
    Datatype.defaultErrorMessages.put(Datatype.NUMERIC_DELIMITED, "default num delim err msg");
    List<Point> temp = Datatype.NUMERIC_DELIMITED.getErrorHighlightPoints(shield, "unitTestNumbersDelimited", "-12345abc,123abc,abc124");
    System.out.println("unitTestNumbersDelimited=" + temp);
  }
  
  @Test
  public void testNumericDelimited() {
    Map<String, String> m = new HashMap<String, String>();
    m.put("unitTestNumericDelimited", ",");
    Datatype.numericDelimiters.put(shield.name, m);

    MockHttpServletRequest req = new MockHttpServletRequest();
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestNumericDelimited", "-12345"));
    assertEquals(false, shield.threat(req, shield.parameters, "unitTestNumericDelimited", "121,23"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestNumericDelimited", "+foobar"));
    assertEquals(true, shield.threat(req, shield.parameters, "unitTestNumericDelimited", "123bar"));
  }

  @Test
  public void testNumericDelimitedType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    Datatype dt = Datatype.NUMERIC_DELIMITED;
    assertEquals(true, dt.inError(req, shield, "unitTestNumericDelimitedNoDelimSet", "12,34,56", 8));

    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimited", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimited", null);
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimitedNoDelimSet", null);
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
  public void testAlphanumericType() {
    Datatype dt = Datatype.ALPHANUMERIC;
    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimited", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimitedNoDelimSet", null);
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestNumericDelimited", "1239.xyz");
    assertEquals(true, list.size() > 0);
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
    Datatype dt = Datatype.ALPHANUMERIC_AND_MORE;
    assertEquals(false, dt.inError(req, shield, "unitTestAlphanumericAndMoreNoDelimSet", "", 0));
    assertEquals(false, dt.inError(req, shield, "unitTestAlphanumericAndMoreNoDelimSet", null, 0));
    assertEquals(false, dt.inError(req, shield, "unitTestAlphanumericAndMoreNoDelimSet", "abcde", 5));
    assertEquals(true, dt.inError(req, shield, "unitTestAlphanumericAndMoreNoDelimSet", "abcde?fg", 8));

    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestAlphanumericAndMoreNoDelimSet", "abcde?fg");
    assertEquals(true, list.size() == 1);
    list = dt.getErrorHighlightPoints(shield, "unitTestAlphanumericAndMoreNoDelimSet", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestAlphanumericAndMoreNoDelimSet", null);
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestAlphanumericAndMore", "1239.xyz");
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
  }

  @Test
  public void testCharType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    Datatype dt = Datatype.CHAR;
    assertEquals(false, dt.inError(req, shield, "unitTestChar", "", 0));
    assertEquals(false, dt.inError(req, shield, "unitTestChar", "", 1));
    assertEquals(true, dt.inError(req, shield, "unitTestChar", "12345", 5));

    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestChar", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestChar", null);
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestChar", "abcdefg");
    assertEquals(true, list.size() == 1);
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
  public void testStringType() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    Datatype dt = Datatype.STRING;
    assertEquals(false, dt.inError(req, shield, "unitTestString", "", 0));

    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestChar", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestChar", null);
    assertEquals(true, list.size() == 0);
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
    Datatype dt = Datatype.REGEX;
    assertEquals(false, dt.inError(req, shield, "unitTestCustomTel", "416-555-5555", 12));
    assertEquals(true, dt.inError(req, shield, "unitTestCustomTel", "abc-def-ghij", 12));
    assertEquals(true, dt.inError(req, shield, "unitTestCustomTel", "a", 1));
    assertEquals(true, dt.inError(req, shield, "unitTestCustomTel", "abc-def-ghij-klmn", 17));

    List<Point> list = dt.getErrorHighlightPoints(shield, "unitTestCustomTel", "");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestCustomTel", null);
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestCustomTel", "416-555-5555");
    assertEquals(true, list.size() == 0);
    list = dt.getErrorHighlightPoints(shield, "unitTestCustomTel", "abc123def456");
    assertEquals(true, list.size() == 1);
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
  }

  @Test
  public void testEnum() {
    Datatype dt = Datatype.NUMERIC;
    assertTrue(Datatype.get(Datatype.TYPE_NUMERIC) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);

    dt = Datatype.NUMERIC_DELIMITED;
    assertTrue(Datatype.get(Datatype.TYPE_NUMERIC_DELIMITED) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, "foo", null).size() == 0);

    dt = Datatype.ALPHANUMERIC;
    assertTrue(Datatype.get(Datatype.TYPE_ALPHANUMERIC) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);

    dt = Datatype.ALPHANUMERIC_AND_MORE;
    assertTrue(Datatype.get(Datatype.TYPE_ALPHANUMERIC_AND_MORE) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);

    dt = Datatype.STRING;
    assertTrue(Datatype.get(Datatype.TYPE_STRING) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);

    dt = Datatype.CHAR;
    assertTrue(Datatype.get(Datatype.TYPE_CHAR) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);

    dt = Datatype.REGEX;
    assertTrue(Datatype.get(Datatype.TYPE_REGEX) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);
    assertTrue(dt.getErrorHighlightPoints(shield, null, "").size() == 0);
    assertTrue(dt.getErrorHighlightPoints(shield, "", null).size() == 0);
    assertTrue(dt.getErrorHighlightPoints(shield, "", "").size() == 0);

    dt = Datatype.JAVA;
    assertTrue(Datatype.get(Datatype.TYPE_JAVA) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);
    assertTrue(dt.getErrorHighlightPoints(shield, "key", "value").size() == 1);

    dt = Datatype.CONSTANT;
    assertTrue(Datatype.get(Datatype.TYPE_CONSTANT) != null);
    assertTrue(dt.getErrorHighlightPoints(shield, null, null).size() == 0);
    assertTrue(dt.getErrorHighlightPoints(shield, "key", "value").size() == 1);
    assertTrue(Datatype.get("k{") != null);

    assertTrue(Datatype.get("foo{") == null);
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
  }

  @Test
  public void testJavaInvalidMethod() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    boolean b = shield.threat(req, shield.parameters, "unitTestJavaInvalidMethod", "0000");
    assertEquals(true, b);
  }
  
  @Test
  public void getInvalidDatatype() {
    Datatype dt = Datatype.get("foobar");
    assertTrue(dt == null);
  }

  @Test
  public void testBrokenConfiguredAlphanumericAndMore() {
    Datatype dt = Datatype.get("unitTestAlphanumericAndMoreInvalidConfig1");
    assertTrue(dt == null);
    dt = Datatype.get("unitTestAlphanumericAndMoreInvalidConfig2");
    assertTrue(dt == null);
    dt = Datatype.get("unitTestAlphanumericAndMoreInvalidConfig3");
    assertTrue(dt == null);
  }
  
  @Test
  public void testBrokenConfiguredNumericDelimited() {
    Datatype dt = Datatype.get("unitTestNumericDelimitedInvalidConfig1");
    assertTrue(dt == null);
    dt = Datatype.get("unitTestNumericDelimitedInvalidConfig2");
    assertTrue(dt == null);
    dt = Datatype.get("unitTestNumericDelimitedInvalidConfig3");
    assertTrue(dt == null);
  }
  
}

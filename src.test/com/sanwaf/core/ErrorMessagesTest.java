package com.sanwaf.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.sanwaf.core.Error;
import com.sanwaf.core.Shield;
import com.sanwaf.core.Sanwaf;

public class ErrorMessagesTest {
  static Sanwaf sanwaf;
  static Shield shield;
  static Shield shieldBadPlaceholders;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-customErrors.xml");
      shield = UnitTestUtil.getShield(sanwaf, "XSS");
      shieldBadPlaceholders = UnitTestUtil.getShield(sanwaf, "BadPlaceholders");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void alphanumericAndMoreMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "AlphanumericAndMore", "abc : ? . 123!");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM Alphanumeric and more") && !error.toJson().contains("[? :]")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void alpahnumericAndMoreDatatatypeErrorMsgTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemAlphanumericAndMore p = new ItemAlphanumericAndMore("", "a{?}", Integer.MAX_VALUE, 0, "", "");
    String s = p.modifyErrorMsg(req, "some {0} String");
    assertTrue(s.contains("?"));
  }
  
  @Test
  public void alphanumericAndMoreMessageNoPlaceholderTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shieldBadPlaceholders, "AlphanumericAndMoreBadPlaceholder", "abc : ? . 123!");
    for (Error error : errors) {
      if (!error.toJson().contains("[0]")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void alphanumericAndMoreSpecialCharsErrorMessgesTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "AlphanumericAndMoreSpecialChars", "^^^");
    assertTrue(errors.size() == 1);
    String error = errors.get(0).toJson();
    assertTrue(error.contains("<space>"));
    assertTrue(error.contains("<tab>"));
    assertTrue(error.contains("<newline>"));
    assertTrue(error.contains("<carriage return>"));
  }

  @Test
  public void numericDelimitedMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "NumericDelimited", "123,456;789");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM Numeric") && !error.toJson().contains("\",\"")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void numericDelimitedMessageNoPlaceholderTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shieldBadPlaceholders, "NumericDelimitedBadPlaceholder", "123,456;789");
    for (Error error : errors) {
      if (!error.toJson().contains("\\\"0\\\"")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void numericDelimietedDatatatypeErrorMsgTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemNumericDelimited p = new ItemNumericDelimited("", "n{,}", Integer.MAX_VALUE, 0, "", "", false);
    String s = p.modifyErrorMsg(req, "some {0} String");
    assertTrue(s.contains(","));
  }
  
  @Test
  public void numericDatatatypeErrorMsgTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Numeric", "-123 456");
    assertTrue(errors.size() > 0);
    assertTrue(errors.get(0).errorPoints.size() > 0);

    req = new MockHttpServletRequest();
    errors = sanwaf.getError(req, shield, "Numeric", "-123..456");
    assertTrue(errors.size() > 0);
    assertTrue(errors.get(0).errorPoints.size() > 0);

    req = new MockHttpServletRequest();
    errors = sanwaf.getError(req, shield, "Numeric", "-123.456");
    assertTrue(errors.size() == 0);
  }
  
  @Test
  public void constantDatatatypeErrorMsgTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    ItemConstant p = new ItemConstant("", "k{foo,bar,far}", Integer.MAX_VALUE, 0, "", "");
    String s = Error.getErrorMessage(req, shield, p);
    assertTrue(s.contains("foo"));
  }

  @Test
  public void regexMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Regex", "some text <script> some other script... <script>");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM Regex")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void javaMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Java", "some text <script> some other script... <script>");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM Java")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void formatMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "Format", "some invalid format");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM Format")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

}

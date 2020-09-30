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
  public void customShieldHighlightXssRegexMatchTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestString", "some text <script> some other script... <script>");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void alphanumericAndMoreMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestAlphanumericAndMore", "abc : ? . 123!");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM") && !error.toJson().contains("[? :]")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void alphanumericAndMoreMessageNoPlaceholderTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shieldBadPlaceholders, "unitTestAlphanumericAndMoreBadPlaceholder", "abc : ? . 123!");
    for (Error error : errors) {
      if (!error.toJson().contains("[0]")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void numericDelimitedMessageTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestNumericDelimited", "123,456;789");
    for (Error error : errors) {
      if (!error.toJson().contains("XSS CUSTOM") && !error.toJson().contains("\",\"")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void numericDelimitedMessageNoPlaceholderTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shieldBadPlaceholders, "unitTestNumericDelimitedBadPlaceholder", "123,456;789");
    for (Error error : errors) {
      if (!error.toJson().contains("\\\"0\\\"")) {
        fail("XSS - Test FAILED.\n" + error.toJson());
      }
    }
  }

  @Test
  public void datatatypeErrorMsgAlpahnumericAndMoreTest() {
    ItemAlphanumericAndMore p = new ItemAlphanumericAndMore("", "a{?}", Integer.MAX_VALUE, 0, "", "");
    String s = p.modifyErrorMsg("some {0} String");
    assertTrue(s.contains("?"));
  }

  @Test
  public void datatatypeErrorMsgNumericDelimietedTest() {
    ItemNumericDelimited p = new ItemNumericDelimited("", "n{,}", Integer.MAX_VALUE, 0, "", "");
    String s = p.modifyErrorMsg("some {0} String");
    assertTrue(s.contains(","));
  }

  @Test
  public void testAlphanumericAndMoreSpecialCharsErrorMessges() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "unitTestAlphanumericAndMoreSpecialChars", "^^^");
    assertTrue(errors.size() == 1);
    String error = errors.get(0).toJson();
    assertTrue(error.contains("<space>"));
    assertTrue(error.contains("<tab>"));
    assertTrue(error.contains("<newline>"));
    assertTrue(error.contains("<carriage return>"));
  }

  @Test
  public void datatatypeErrorMsgConstantTest() {
    ItemConstant p = new ItemConstant("", "k{foo,bar,far}", Integer.MAX_VALUE, 0, "", "");
    String s = Error.getErrorMessage(shield, p);
    assertTrue(s.contains("foo"));

  }
}

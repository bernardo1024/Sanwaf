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

public class ErrorMessagesParmsTest {
  static Sanwaf sanwaf;
  static Shield shield;
  static Shield shieldBadPlaceholders;

  @BeforeClass
  public static void setUpClass() {
    try {
      sanwaf = new Sanwaf(new UnitTestLogger(), "/sanwaf-customErrorsParms.xml");
      shield = UnitTestUtil.getShield(sanwaf, "XSS");
    } catch (IOException ioe) {
      assertTrue(false);
    }
  }

  @Test
  public void customShieldErrorMsgTest() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    List<Error> errors = sanwaf.getError(req, shield, "string", "some text <script> some other script... <script>");
    if (!isKeywordFound(errors, "string error msg")) {
      fail("Parm error failed for: String");
    }

    errors = sanwaf.getError(req, shield, "numeric", "abcde");
    if (!isKeywordFound(errors, "numeric error msg")) {
      fail("Parm error failed for: numeric");
    }

    errors = sanwaf.getError(req, shield, "numericSized", "abcde");
    if (!isKeywordFound(errors, "numeric sized error msg")) {
      fail("Parm error failed for: numericSized");
    }

    errors = sanwaf.getError(req, shield, "numericDelim", "abcde");
    if (!isKeywordFound(errors, "numeric delimited error message ")) {
      fail("Parm error failed for: numericDelim");
    }

    errors = sanwaf.getError(req, shield, "alphanumericSized", "()_&^%");
    if (!isKeywordFound(errors, "alphanumeric sized error message")) {
      fail("Parm error failed for: alphanumericSized");
    }

    errors = sanwaf.getError(req, shield, "alphanumericAndMoreSized", "!@#$%^&*");
    if (!isKeywordFound(errors, "alphanumeric and more error message ")) {
      fail("Parm error failed for: alphanumericAndMoreSized");
    }

    errors = sanwaf.getError(req, shield, "constant", "abcde");
    if (!isKeywordFound(errors, "constant error message ")) {
      fail("Parm error failed for: constant");
    }

    errors = sanwaf.getError(req, shield, "regex", "abcde");
    if (!isKeywordFound(errors, "custom regex error message")) {
      fail("Parm error failed for: regex");
    }

    errors = sanwaf.getError(req, shield, "char", "abcde");
    if (!isKeywordFound(errors, "char error message")) {
      fail("Parm error failed for: char");
    }

    errors = sanwaf.getError(req, shield, "java", "00001");
    if (!isKeywordFound(errors, "java error message")) {
      fail("Parm error failed for: java");
    }

    errors = sanwaf.getError(req, shield, "aHeaderNumber", "abcde");
    if (!isKeywordFound(errors, "header error msg")) {
      fail("Parm error failed for: aHeaderNumber");
    }

    errors = sanwaf.getError(req, shield, "aCookieAlphanumericAndMore", "!@#$%");
    if (!isKeywordFound(errors, "cookie error msg")) {
      fail("Parm error failed for: aCookieNumber");
    }
  }

  private static boolean isKeywordFound(List<Error> errors, String keyword) {
    if (errors == null || errors.size() < 1) {
      return false;
    }

    for (Error error : errors) {
      if (!error.toJson().contains(keyword)) {
        return false;
      }
    }
    return true;
  }
}

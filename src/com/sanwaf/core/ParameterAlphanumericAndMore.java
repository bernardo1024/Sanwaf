package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ParameterAlphanumericAndMore extends ParameterAlphanumeric {
  static final String TYPE_ALPHANUMERIC_AND_MORE_SPACE = "\\s";
  static final String TYPE_ALPHANUMERIC_AND_MORE_TAB = "\\t";
  static final String TYPE_ALPHANUMERIC_AND_MORE_NEWLINE = "\\n";
  static final String TYPE_ALPHANUMERIC_AND_MORE_CR = "\\r";
  static final String TYPE_ALPHANUMERIC_AND_MORE_SPACE_LONG = "<space>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_TAB_LONG = "<tab>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_NEWLINE_LONG = "<newline>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_CR_LONG = "<carriage return>";

  char[] alphanumericAndMoreChars = null;

  ParameterAlphanumericAndMore(String name, String type, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    this.type = Metadata.TYPE_ALPHANUMERIC_AND_MORE;
    parseMoreChars(type);
  }

  private void parseMoreChars(String value) {
    int start = value.indexOf(Metadata.SEP_START_CHAR);
    if (start > 0) {
      int end = value.lastIndexOf(Metadata.SEP_END_CHAR);
      if (end > start) {
        // handle special chars
        char[] array = getAlphaNumericAndMoreCharArray(value.substring(start + Metadata.SEP_START_CHAR.length(), end));
        alphanumericAndMoreChars = array;
      }
    }
    if (alphanumericAndMoreChars == null) {
      alphanumericAndMoreChars = new char[0];
    }
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value == null) {
      return points;
    }
    int start = -1;
    int len = value.length();
    int i = 0;
    for (i = 0; i < len; i++) {
      char c = value.charAt(i);
      if (isCharNotAlphanumeric(c)) {
        boolean pass = false;
        for (char moreChar : alphanumericAndMoreChars) {
          if (c == moreChar) {
            pass = true;
            break;
          }
        }
        if (!pass) {
          if (start < 0) {
            start = i;
          }
        } else {
          if (start >= 0) {
            points.add(new Point(start, i));
            start = -1;
          }
        }
      } else {
        if (start >= 0) {
          points.add(new Point(start, i));
          start = -1;
        }
      }
    }
    if (start >= 0) {
      points.add(new Point(start, i));
    }
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (value == null) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (isCharNotAlphanumeric(c)) {
        boolean pass = false;
        for (char cs : alphanumericAndMoreChars) {
          if (c == cs) {
            pass = true;
            break;
          }
        }
        if (!pass) {
          return true;
        }
      }
    }
    return false;
  }

  public String substituteAlphaNumericAndMoreChars(String errorString) {
    int i = errorString.indexOf(Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      return errorString.substring(0, i) + Metadata.jsonEncode(handleSpecialChars(alphanumericAndMoreChars))
          + errorString.substring(i + Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }

  static String handleSpecialChars(char[] chars) {
    String s = String.valueOf(chars);
    s = replaceString(s, " ", TYPE_ALPHANUMERIC_AND_MORE_SPACE_LONG);
    s = replaceString(s, "\t", TYPE_ALPHANUMERIC_AND_MORE_TAB_LONG);
    s = replaceString(s, "\n", TYPE_ALPHANUMERIC_AND_MORE_NEWLINE_LONG);
    s = replaceString(s, "\r", TYPE_ALPHANUMERIC_AND_MORE_CR_LONG);
    return s;
  }

  static char[] getAlphaNumericAndMoreCharArray(String s) {
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_SPACE, " ");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_TAB, "\t");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_NEWLINE, "\n");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_CR, "\r");
    return s.toCharArray();
  }

  static String replaceString(String s, String from, String to) {
    int i = s.indexOf(from);
    if (i >= 0) {
      s = s.substring(0, i) + to + s.substring(i + from.length(), s.length());
    }
    return s;
  }

}


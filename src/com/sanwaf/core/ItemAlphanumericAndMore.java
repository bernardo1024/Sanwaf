package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemAlphanumericAndMore extends ItemAlphanumeric {
  static final String INVALID_AN_MORE = "Invalid Alphanumeric And More: ";
  static final String SPACE = "\\s";
  static final String TAB = "\\t";
  static final String NEWLINE = "\\n";
  static final String CARRIAGE_RETURN = "\\r";
  static final String SPACE_LONG = "<space>";
  static final String TAB_LONG = "<tab>";
  static final String NEWLINE_LONG = "<newline>";
  static final String CARRIAGE_RETURN_LONG = "<carriage return>";

  char[] moreChars = new char[0];

  ItemAlphanumericAndMore(ItemData id) {
    super(id);
    setMoreChars(id.type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value == null || maskError.length() > 0) {
      return points;
    }
    int start = -1;
    int len = value.length();
    for (int i = 0; i < len; i++) {
      char c = value.charAt(i);
      if (isNotAlphanumeric(c)) {
        start = processNotAlphanumeric(points, start, i, c);
      } else {
        if (start >= 0) {
          points.add(new Point(start, i));
          start = -1;
        }
      }
    }
    if (start >= 0) {
      points.add(new Point(start, len));
    }
    return points;
  }

  private int processNotAlphanumeric(List<Point> points, int start, int i, char c) {
    if (!isInMoreChars(c)) {
      if (start < 0) {
        start = i;
      }

    } else {
      if (start >= 0) {
        points.add(new Point(start, i));
        start = -1;
      }
    }
    return start;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    DefinitiveError definitiveError = getDefiniteError(req, value);
    if(definitiveError != null) {
      return definitiveError.error;
    }
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (isNotAlphanumeric(c) && !isInMoreChars(c)) {
        return handleMode(true, value, INVALID_AN_MORE + String.valueOf(moreChars), req);
      }
    }
    return false;
  }

  private boolean isInMoreChars(char c) {
    for (char more : moreChars) {
      if (c == more) {
        return true;
      }
    }
    return false;
  }

  @Override
  String modifyErrorMsg(ServletRequest req, String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(handleSpecialChars(moreChars)) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }

  static String handleSpecialChars(char[] chars) {
    String s = String.valueOf(chars);
    s = replaceString(s, " ", SPACE_LONG);
    s = replaceString(s, "\t", TAB_LONG);
    s = replaceString(s, "\n", NEWLINE_LONG);
    s = replaceString(s, "\r", CARRIAGE_RETURN_LONG);
    return s;
  }

  static char[] getMoreCharArray(String s) {
    s = replaceString(s, SPACE, " ");
    s = replaceString(s, TAB, "\t");
    s = replaceString(s, NEWLINE, "\n");
    s = replaceString(s, CARRIAGE_RETURN, "\r");
    return s.toCharArray();
  }

  static String replaceString(String s, String from, String to) {
    int i = s.indexOf(from);
    if (i >= 0) {
      s = s.substring(0, i) + to + s.substring(i + from.length(), s.length());
    }
    return s;
  }

  private void setMoreChars(String value) {
    int start = value.indexOf(ItemFactory.SEP_START);
    int end = value.lastIndexOf(ItemFactory.SEP_END);
    char[] array = getMoreCharArray(value.substring(start + ItemFactory.SEP_START.length(), end));
    moreChars = array;
  }

  @Override 
  Types getType() {
    return Types.ALPHANUMERIC_AND_MORE;
  }
}

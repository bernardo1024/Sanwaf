package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemFormat extends Item {
  String formatString = null;

  ItemFormat(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = FORMAT;
    setFormat(type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value.length() == 0) {
      return points;
    }
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!required && value.length() == 0) {
      return false;
    }
    String thisFormat = escapedChars(formatString);
    if (value.length() != thisFormat.length()) {
      return true;
    }

    for (int i = 0; i < value.length(); i++) {
      char f = thisFormat.charAt(i);
      char c = value.charAt(i);
      if ((f == '#' && c >= '0' && c <= '9') || 
          ((f == 'A' || f == 'c') && c >= 'A' && c <= 'Z') || 
          ((f == 'a' || f == 'c')  && c >= 'a' && c <= 'z') ) {
        continue;
      }
      if (c != unEscapedChar(f)) {
        return true;
      }
    }
    return false;
  }

  private String escapedChars(String s){
    s = s.replaceAll("\\\\#", "\t");
    s = s.replaceAll("\\\\A", "\n");
    s = s.replaceAll("\\\\a", "\r");
    s = s.replaceAll("\\\\c", "\f");
    return s;
  }

  private char unEscapedChar(char c){
    if(c == '\t') {
      return '#';
    } else if (c == '\n') {
      return 'A';
    } else if (c == '\r') {
      return 'a';
    } else if (c == '\f') {
      return 'c';
    }
    return c;
  }

  @Override
  String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(formatString) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void setFormat(String value) {
    int start = value.indexOf(FORMAT);
    if (start >= 0) {
      formatString = value.substring(start + FORMAT.length(), value.length() - 1);
    }
  }
}

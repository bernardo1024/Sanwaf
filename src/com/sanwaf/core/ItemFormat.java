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
    if (value.length() != formatString.length()) {
      return true;
    }

    for (int i = 0; i < value.length(); i++) {
      char f = formatString.charAt(i);
      char c = value.charAt(i);
      if ((f == '#' && c >= '0' && c <= '9') || (f == 'A' && c >= 'A' && c <= 'Z') || (f == 'a' && c >= 'a' && c <= 'z')) {
        continue;
      }
      if (c != f) {
        return true;
      }
    }
    return false;
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

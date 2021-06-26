package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemNumericDelimited extends ItemNumeric {
  String delimiter = "";

  ItemNumericDelimited(String name, String type, int max, int min, String msg, String uri, boolean isInt) {
    super(name, max, min, msg, uri, isInt);
    this.type = NUMERIC_DELIMITED;
    setDelimiter(type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();

    if (value != null) {
      String[] ns = value.split(delimiter, -1);
      for (String n : ns) {
        if (n.length() > 0) {
          points.addAll(super.getErrorPoints(shield, n));
        }
      }
    }
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    String[] ns = value.split(delimiter);
    for (String n : ns) {
      if (super.inError(req, shield, n)) {
        return true;
      }
    }
    return false;
  }

  @Override
  String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(delimiter) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void setDelimiter(String value) {
    int start = value.indexOf(SEP_START);
    int end = value.lastIndexOf(SEP_END);
    delimiter = value.substring(start + SEP_START.length(), end);
  }
}

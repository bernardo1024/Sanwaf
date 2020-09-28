package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ParameterNumericDelimited extends ParameterNumeric {
  String numericDelimiter = "";

  ParameterNumericDelimited(String name, String type, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    this.type = Metadata.TYPE_NUMERIC_DELIMITED;
    parseAlphaNumericAndMoreChars(type);
  }

  private void parseAlphaNumericAndMoreChars(String value) {
    int start = value.indexOf(Metadata.SEP_START_CHAR);
    if (start > 0) {
      int end = value.lastIndexOf(Metadata.SEP_END_CHAR);
      if (end > start) {
        numericDelimiter = value.substring(start + Metadata.SEP_START_CHAR.length(), end);
      }
    }
  }

  @Override
  public List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();

    if (value != null) {
      String[] ns = value.split(numericDelimiter, -1);
      for (String n : ns) {
        if (n.length() > 0) {
          points.addAll(super.getErrorPoints(shield, n));
        }
      }
    }
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    String[] ns = splitNumericDelimited(value);
    for (String n : ns) {
      if (super.inError(req, shield, n)) {
        return true;
      }
    }
    return false;
  }

  String[] splitNumericDelimited(String value) {
    String[] array;
    array = value.split(numericDelimiter);
    return array;
  }

  @Override
  public String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(numericDelimiter) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER.length(), errorMsg.length());
    }
    return errorMsg;
  }
}

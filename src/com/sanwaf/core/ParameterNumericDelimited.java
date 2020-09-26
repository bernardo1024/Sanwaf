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
  public boolean isSizeError(String value) {
    String[] array = splitNumericDelimited(value);
    for (String n : array) {
      if (n.length() < min || n.length() > max) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();

    if (value != null) {
      String[] ns = value.split(numericDelimiter, -1);
      for (String n : ns) {
        if (n.length() > 0) {
          points.addAll(super.getErrorHighlightPoints(shield, n));
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

  String substituteNumericDelimiter(String errorString) {
    int i = errorString.indexOf(Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      return errorString.substring(0, i) + Metadata.jsonEncode(numericDelimiter) + errorString.substring(i + Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }
}


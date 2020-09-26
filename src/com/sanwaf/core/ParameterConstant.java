package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;

final class ParameterConstant extends Parameter {
  List<String> constantValues = null;

  ParameterConstant(String name, String type, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    this.type = Metadata.TYPE_CONSTANT;
    addConstantToType(type);
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    return !constantValues.contains(value);
  }

  private void addConstantToType(String value) {
    int start = value.indexOf(Metadata.TYPE_CONSTANT);
    if (start >= 0) {
      String s = value.substring(start + Metadata.TYPE_CONSTANT.length(), value.length() - 1);
      constantValues = new ArrayList<>(Arrays.asList(s.split(",")));
    }
  }

  String substituteConstantValues(String errorString) {
    int i = errorString.indexOf(Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      return errorString.substring(0, i) + Metadata.jsonEncode(constantValues.toString()) + errorString.substring(i + Error.XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }
}

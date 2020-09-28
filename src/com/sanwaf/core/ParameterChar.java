package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ParameterChar extends Parameter {
  ParameterChar(String name, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    type = Metadata.TYPE_CHAR;
  }

  @Override
  public List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (value == null) {
      return false;
    }
    return (value.length() > 1);
  }
}

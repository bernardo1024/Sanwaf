package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

class ParameterAlphanumeric extends Parameter {
  ParameterAlphanumeric(String name, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    type = Metadata.TYPE_ALPHANUMERIC;
  }

  @Override
  public List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    int start = -1;
    int len = value.length();
    int i = 0;
    for (i = 0; i < len; i++) {
      char c = value.charAt(i);
      if (isCharNotAlphanumeric(c)) {
        if (start < 0) {
          start = i;
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
    if(isSizeError(value)) {
      return true;
    }
    int i = 0;
    for (i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (isCharNotAlphanumeric(c)) {
        return true;
      }
    }
    return false;
  }

  static boolean isCharNotAlphanumeric(char c) {
    return (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a);
  }

}

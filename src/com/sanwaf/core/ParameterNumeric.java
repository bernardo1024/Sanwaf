package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

class ParameterNumeric extends Parameter {
  ParameterNumeric(String name, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    type = Metadata.TYPE_NUMERIC;
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    final int len = value.length();
    int errStart = -1;
    boolean foundDot = false;
    boolean foundNeg = false;
    int i = 0;
    for (i = 0; i < len; i++) {
      char c = value.charAt(i);
      int d = c - '0';
      if (d < 0 || d > 9) {
        if (!foundDot && c == '.') {
          foundDot = true;
        } else {
          if (!foundNeg && i == 0 && c == '-') {
            foundNeg = true;
          }
          if (errStart < 0) {
            errStart = i;
          }
        }
      } else {
        if (errStart >= 0) {
          points.add(new Point(errStart, i));
          errStart = -1;
        }
      }
    }
    if (errStart >= 0) {
      points.add(new Point(errStart, i));
    }
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    boolean foundDot = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      int d = c - '0';
      if (d < 0 || d > 9) {
        if (i == 0 && c == '-') {
          continue;
        } else if (c == '.' && !foundDot) {
          foundDot = true;
        } else {
          return true;
        }
      }
    }
    return false;
  }

}


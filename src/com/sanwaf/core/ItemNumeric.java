package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

class ItemNumeric extends Item {
  ItemNumeric(String name, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    type = NUMERIC;
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    final int len = value.length();
    int errStart = -1;
    boolean foundDot = false;
    boolean foundNeg = false;
    for (int i = 0; i < len; i++) {
      char c = value.charAt(i);
      int d = c - '0';
      if (d < 0 || d > 9) {
        if (!foundDot && c == '.') {
          foundDot = true;
        } else {
          if (i == 0 && !foundNeg && c == '-') {
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
      points.add(new Point(errStart, len));
    }
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return false;
    }
    if (isSizeError(value)) {
      return true;
    }
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

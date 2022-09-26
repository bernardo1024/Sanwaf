package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

class ItemAlphanumeric extends Item {
  ItemAlphanumeric(ItemData id) {
    super(id);
  }

  @Override
  List<Point> getErrorPoints(Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if(maskError.length() > 0) {
      return points;
    }
    int start = -1;
    int len = value.length();
    for (int i = 0; i < len; i++) {
      if (isNotAlphanumeric(value.charAt(i))) {
        if (start < 0) {
          start = i;
        }
      } else {
        if (start >= 0 || i == len - 1) {
          points.add(new Point(start, i));
          start = -1;
        }
      }
    }
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return handleMode(true, value);
    }
    if (isSizeError(value)) {
      return handleMode(true, value);
    }
    if(value.length() == 0) {
      return false;
    }
    int i = 0;
    for (i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (isNotAlphanumeric(c)) {
        return handleMode(true, value);
      }
    }
    return false;
  }

  static boolean isNotAlphanumeric(char c) {
    return (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a);
  }

  @Override 
  Types getType() {
    return Types.ALPHANUMERIC_AND_MORE;
  }
}

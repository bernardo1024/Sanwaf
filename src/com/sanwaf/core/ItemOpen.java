package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemOpen extends Item {
  ItemOpen(String name, String display, int max, int min, String msg, String uri) {
    super(name, display, max, min, msg, uri);
    type = OPEN;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return true;
    }
    if (isSizeError(value)) {
      return true;
    }
    if (value == null) {
      return false;
    }
    return (value.length() > 1);
  }

  @Override
  List<Point> getErrorPoints(Shield shield, String value) {
    List<Point> points = new ArrayList<>();
    if(maskError.length() > 0) {
      return points;
    }
    points.add(new Point(0, value.length()));
    return points;
  }
}

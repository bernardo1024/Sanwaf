package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemChar extends Item {
  ItemChar(ItemData id) {
    super(id);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return handleMode(true, value);
    }
    if (isSizeError(value)) {
      return handleMode(true, value);
    }
    if (value == null) {
      return false;
    }
    return handleMode((value.length() > 1), value);
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

  @Override 
  Types getType() {
    return Types.CHAR;
  }
}

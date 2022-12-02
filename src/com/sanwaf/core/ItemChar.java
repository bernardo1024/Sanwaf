package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemChar extends Item {
  static final String INVALID_CHAR = "Invalid Constant: ";

  ItemChar(ItemData id) {
    super(id);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value, boolean doAllBlocks) {
    ModeError me = isModeError(req, value);
    if (me != null) {
      return returnBasedOnDoAllBlocks(handleMode(me.error, value, INVALID_CHAR, req), doAllBlocks);
    }
    if (value == null) {
      return false;
    }
    return returnBasedOnDoAllBlocks(handleMode((value.length() > 1), value, INVALID_CHAR, req), doAllBlocks);
  }

  @Override
  List<Point> getErrorPoints(Shield shield, String value) {
    List<Point> points = new ArrayList<>();
    if (maskError.length() > 0) {
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

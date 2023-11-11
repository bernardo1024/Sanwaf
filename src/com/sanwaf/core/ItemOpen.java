package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletRequest;

class ItemOpen extends Item {
  ItemOpen(ItemData id) {
    super(id);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value, boolean doAllBlocks) {
    ModeError me = isModeError(req, value);
    if (me != null) {
      return handleMode(me.error, value, req, mode, true);
    }
    return false;
  }

  @Override
  List<Point> getErrorPoints(Shield shield, String value) {
    if (maskError.length() > 0) {
      return new ArrayList<>();
    }
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  Types getType() {
    return Types.OPEN;
  }
}


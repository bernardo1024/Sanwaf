package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

final class ItemString extends Item {

  ItemString() {
    type = STRING;
  }

  ItemString(String name, String display, int max, int min, String msg, String uri) {
    super(name, display, max, min, msg, uri);
    type = STRING;
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if(maskError.length() > 0) {
      return points;
    }

    for (Pattern p : shield.patterns) {
      Matcher m = p.matcher(value);
      while (m.find()) {
        int start = m.start();
        int end = m.end();
        points.add(new Point(start, end));
      }
    }
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return true;
    }
    if (isSizeError(value)) {
      return true;
    }
    if(value.length() == 0) {
      return false;
    }
    for (Pattern p : shield.patterns) {
      if (p.matcher(value).find()) {
        return true;
      }
    }
    return false;
  }
}

package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

final class ParameterString extends Parameter {

  ParameterString() {
    type = Metadata.TYPE_STRING;
  }

  ParameterString(String name, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    type = Metadata.TYPE_STRING;
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();

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
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    for (Pattern p : shield.patterns) {
      if (p.matcher(value).find()) {
        return true;
      }
    }
    return false;
  }
}


package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

final class ParameterRegex extends Parameter {
  String patternName = null;
  Pattern pattern = null;

  ParameterRegex(String name, String type, int max, int min, String errorMsg, String path) {
    super(name, max, min, errorMsg, path);
    this.type = Metadata.TYPE_REGEX;
    addRegexParmToType(type);
  }

  @Override
  public List<Point> getErrorHighlightPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value == null || value.length() == 0) {
      return points;
    }
    Matcher m = pattern.matcher(value);
    if (!m.find()) {
      points.add(new Point(0, value.length()));
    }
    return points;
  }

  @Override
  public boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (pattern == null) {
      pattern = shield.customPatterns.get(patternName);
    }
    return !pattern.matcher(value).find();
  }

  private void addRegexParmToType(String value) {
    int start = value.indexOf(Metadata.TYPE_REGEX);
    if (start >= 0) {
      patternName = value.substring(start + Metadata.TYPE_REGEX.length(), value.length() - 1).toLowerCase();
    }
  }
}


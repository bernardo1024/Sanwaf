package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

final class ItemRegex extends Item {
  String patternName = null;
  Pattern pattern = null;

  ItemRegex(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = REGEX;
    setPattern(type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
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
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (pattern == null) {
      pattern = shield.customPatterns.get(patternName);
    }
    if (!isUriValid(req)) {
      return true;
    }
    if (isSizeError(value)) {
      return true;
    }
    if(value.length() == 0) {
      return false;
    }
    return !pattern.matcher(value).find();
  }

  private void setPattern(String value) {
    if (value.startsWith(INLINE_REGEX)) {
      pattern = Pattern.compile(value.substring(INLINE_REGEX.length(), value.length() - 1), Pattern.CASE_INSENSITIVE);
    } else {
      int start = value.indexOf(REGEX);
      if (start >= 0) {
        patternName = value.substring(start + REGEX.length(), value.length() - 1).toLowerCase();
      }
    }
  }
}

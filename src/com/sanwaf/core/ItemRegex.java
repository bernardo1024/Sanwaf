package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

final class ItemRegex extends Item {
  static final String FAILED_CUSTOM_PATTERN = "Failed Custom Pattern: ";
  String patternName = null;
  Rule rule = null;

  ItemRegex(ItemData id) {
    super(id);
    setPattern(id.type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value == null || value.length() == 0 || maskError.length() > 0) {
      return points;
    }
    if (rule == null) {
      rule = shield.customRulePatterns.get(patternName);
    }
    Matcher m = rule.pattern.matcher(value);
    if (!m.find()) {
      points.add(new Point(0, value.length()));
    }
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (mode == Modes.DISABLED) { return false; }
    if (!isUriValid(req)) {
      return handleMode(true, value, INVALID_URI, req);
    }
    if (isSizeError(value)) {
      return handleMode(true, value, INVALID_SIZE, req);
    }
    if(value.length() == 0) {
      return false;
    }
    if (rule == null) {
      rule = shield.customRulePatterns.get(patternName);
    }
    if(rule.mode == Modes.DISABLED) {
      return false;
    }

    if(!rule.pattern.matcher(value).find()) {
      if(mode == Modes.DETECT || mode == Modes.DETECT_ALL) {
        handleMode(true, value, FAILED_CUSTOM_PATTERN + patternName, req);
      }
      if(rule.mode == Modes.BLOCK && mode == Modes.BLOCK) {
        return true;
      }
    }
    return false;
  }

  private void setPattern(String value) {
    if (value.startsWith(ItemFactory.INLINE_REGEX)) {
      rule = new Rule();
      rule.pattern = Pattern.compile(value.substring(ItemFactory.INLINE_REGEX.length(), value.length() - 1), Pattern.CASE_INSENSITIVE);
      patternName = "inline-regex: " + rule.pattern;
    } else {
      int start = value.indexOf(ItemFactory.REGEX);
      if (start >= 0) {
        patternName = value.substring(start + ItemFactory.REGEX.length(), value.length() - 1).toLowerCase();
      }
    }
  }

  @Override 
  Types getType() {
    return Types.REGEX;
  }
}

package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.ServletRequest;

final class ItemString extends Item {

  ItemString() {}

  ItemString(ItemData id) {
    super(id);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if(maskError.length() > 0) {
      return points;
    }
    for (Map.Entry<String, Rule> r : shield.rulePatterns.entrySet()) {
      Matcher m = r.getValue().pattern.matcher(value);
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
      return handleMode(true, value);
    }
    if (isSizeError(value)) {
      return handleMode(true, value);
    }
    if(value.length() == 0) {
      return false;
    }
    
    boolean inError = false;
    for (Map.Entry<String, Rule> rule : shield.rulePatterns.entrySet()) {
      if (rule.getValue().mode == Modes.DISABLED) { continue; }

      if (rule.getValue().pattern.matcher(value).find()) {
        inError = true;
        if(rule.getValue().mode != Modes.BLOCK) { handleMode(true, value); }
        if(rule.getValue().mode != Modes.DETECT_ALL) { break; }
      } 
    }
    if(mode == Modes.DETECT || mode == Modes.DETECT_ALL) {
      return false;
    }
    return inError;
  }

  @Override 
  Types getType() {
    return Types.STRING;
  }
}

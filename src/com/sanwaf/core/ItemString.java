package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.ServletRequest;

final class ItemString extends Item {
  static final String FAILED_PATTERN = "Failed Pattern: ";
  static final String MATCHED_PATTERN = "Matched Pattern: ";

  ItemString() {
  }

  ItemString(ItemData id) {
    super(id);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (shield == null || maskError.length() > 0) {
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
    ModeError me = isModeError(req, value);
    if (me != null) {
      return handleMode(me.error, value, FAILED_PATTERN, req);
    }
    boolean inError = false;
    if (shield != null) {
      //first process the detects & detect all - ignore the return value for detect
      inError = isInErrorForPatterns(req, shield, value, Modes.DETECT, inError);
      //then do the blocks
      inError = isInErrorForPatterns(req, shield, value, Modes.BLOCK, inError);
    }
    if (mode == Modes.DETECT || mode == Modes.DETECT_ALL) {
      return false;
    }
    return inError;
  }

  private boolean isInErrorForPatterns(final ServletRequest req, final Shield shield, final String value, Modes mode, boolean inError) {
    for (Map.Entry<String, Rule> rule : shield.rulePatterns.entrySet()) {
      Modes ruleMode = rule.getValue().mode;
      if (ruleMode == Modes.DISABLED ||
          (mode == Modes.DETECT && ruleMode == Modes.BLOCK) ||
          (mode == Modes.BLOCK && (ruleMode == Modes.DETECT || ruleMode == Modes.DETECT_ALL )) ) {
        continue;
      }
      
      boolean match = rule.getValue().pattern.matcher(value).find();
      if ((rule.getValue().failOnMatch && match) || (!rule.getValue().failOnMatch && !match)) {
        if (rule.getValue().mode == Modes.BLOCK) {
          inError = true;
          handleMode(true, value, FAILED_PATTERN + rule.getKey(), req);
        } else {
          handleMode(true, value, MATCHED_PATTERN + rule.getKey() + " (" + ruleMode + ")", req, ruleMode, true);
        }
        if (mode != Modes.DETECT_ALL && ruleMode != Modes.DETECT_ALL) {
          break;
        }
      }
    }
    return inError;
  }

  @Override
  Types getType() {
    return Types.STRING;
  }
}

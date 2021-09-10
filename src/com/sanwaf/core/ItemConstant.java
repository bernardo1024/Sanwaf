package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemConstant extends Item {
  List<String> constants = null;

  ItemConstant(String name, String display, String type, int max, int min, String msg, String uri) {
    super(name, display, max, min, msg, uri);
    this.type = CONSTANT;
    setConstants(type);
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
    return !constants.contains(value);
  }

  @Override
  String modifyErrorMsg(ServletRequest req, String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(constants.toString()) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
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

  private void setConstants(String value) {
    int start = value.indexOf(CONSTANT);
    if (start >= 0) {
      String s = value.substring(start + CONSTANT.length(), value.length() - 1);
      constants = new ArrayList<>(Arrays.asList(s.split(",")));
    }
  }
}

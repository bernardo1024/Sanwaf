package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemConstant extends Item {
  List<String> constants = null;

  ItemConstant(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = CONSTANT;
    setConstants(type);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return false;
    }
    if (isSizeError(value)) {
      return true;
    }
    return !constants.contains(value);
  }

  @Override
  String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(constants.toString()) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void setConstants(String value) {
    int start = value.indexOf(CONSTANT);
    if (start >= 0) {
      String s = value.substring(start + CONSTANT.length(), value.length() - 1);
      constants = new ArrayList<>(Arrays.asList(s.split(",")));
    }
  }
}

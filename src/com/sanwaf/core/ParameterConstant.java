package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;

final class ParameterConstant extends Parameter {
  List<String> constantValues = null;

  ParameterConstant(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = Metadata.TYPE_CONSTANT;
    addConstantToType(type);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if(!isPathValid(req)) { return false; }
    if(isSizeError(value)) { return true; }
    return !constantValues.contains(value);
  }

  private void addConstantToType(String value) {
    int start = value.indexOf(Metadata.TYPE_CONSTANT);
    if (start >= 0) {
      String s = value.substring(start + Metadata.TYPE_CONSTANT.length(), value.length() - 1);
      constantValues = new ArrayList<>(Arrays.asList(s.split(",")));
    }
  }

  @Override
  String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(constantValues.toString()) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER.length(), errorMsg.length());
    }
    return errorMsg;
  }
}

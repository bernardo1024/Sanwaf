package com.sanwaf.core;

import javax.servlet.ServletRequest;

final class ParameterChar extends Parameter {
  ParameterChar(String name, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    type = Metadata.TYPE_CHAR;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if(!isPathValid(req)) { return false; }
    if(isSizeError(value)) { return true; }
    if (value == null) { return false; }
    return (value.length() > 1);
  }
}

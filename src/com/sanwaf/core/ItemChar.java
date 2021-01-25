package com.sanwaf.core;

import javax.servlet.ServletRequest;

final class ItemChar extends Item {
  ItemChar(String name, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    type = CHAR;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return false;
    }
    if (isSizeError(value)) {
      return true;
    }
    if (value == null) {
      return false;
    }
    return (value.length() > 1);
  }
}

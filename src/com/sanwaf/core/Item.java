package com.sanwaf.core;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

abstract class Item {
  static final String NUMERIC = "n";
  static final String NUMERIC_DELIMITED = "n{";
  static final String ALPHANUMERIC = "a";
  static final String ALPHANUMERIC_AND_MORE = "a{";
  static final String STRING = "s";
  static final String CHAR = "c";
  static final String REGEX = "r{";
  static final String JAVA = "j{";
  static final String CONSTANT = "k{";
  static final String SEP_START = "{";
  static final String SEP_END = "}";

  String name;
  String type = null;
  int max = Integer.MAX_VALUE;
  int min = 0;
  String msg = null;
  String[] uri = null;

  Item() {
  }

  Item(String name, int max, int min, String msg, String uri) {
    this.name = name;
    this.max = max;
    this.min = min;
    this.msg = msg;
    setUri(uri);
  }

  abstract boolean inError(ServletRequest req, Shield shield, String value);
  abstract List<Point> getErrorPoints(Shield shield, String value);


  static Item getItem(String name, Item item) {
    item.name = name;
    return item;
  }
  

  static Item getItem(String name, String type, int min, int max, String msg, String uri) {
    Item item = null;
    String t = type.toLowerCase();
    int pos = t.indexOf(SEP_START);
    if (pos > 0) {
      t = t.substring(0, pos + SEP_START.length());
    }

    if (t.equals(NUMERIC)) {
      item = new ItemNumeric(name, max, min, msg, uri);
    } else if (t.equals(NUMERIC_DELIMITED)) {
      type = ensureTypeFormat(type);
      item = new ItemNumericDelimited(name, type, max, min, msg, uri);
    } else if (t.equals(ALPHANUMERIC)) {
      item = new ItemAlphanumeric(name, max, min, msg, uri);
    } else if (t.equals(ALPHANUMERIC_AND_MORE)) {
      type = ensureTypeFormat(type);
      item = new ItemAlphanumericAndMore(name, type, max, min, msg, uri);
    } else if (t.equals(CHAR)) {
      item = new ItemChar(name, max, min, msg, uri);
    } else if (t.equals(REGEX)) {
      type = ensureTypeFormat(type);
      item = new ItemRegex(name, type, max, min, msg, uri);
    } else if (t.equals(JAVA)) {
      type = ensureTypeFormat(type);
      item = new ItemJava(name, type, max, min, msg, uri);
    } else if (t.equals(CONSTANT)) {
      type = ensureTypeFormat(type);
      item = new ItemConstant(name, type, max, min, msg, uri);
    } else {
      item = new ItemString(name, max, min, msg, uri);
    }
    return item;
  }
  
  private static String ensureTypeFormat(String type) {
    if(!type.endsWith(SEP_END)){
      return type + SEP_END;
    }
    return type;
  }

  boolean isUriValid(ServletRequest req) {
    if (uri == null) {
      return true;
    }
    String reqUri = ((HttpServletRequest) req).getRequestURI();
    for (String u : uri) {
      if (u.equals(reqUri)) {
        return true;
      }
    }
    return false;
  }

  boolean isSizeError(String value) {
    if (value == null) {
      return min != 0;
    } else {
      return (value.length() < min || value.length() > max);
    }
  }

  String modifyErrorMsg(String errorMsg) {
    return errorMsg;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("type: ").append(type);
    sb.append(", max: ").append(max);
    sb.append(", min: ").append(min);
    sb.append(", msg: ").append(msg);
    sb.append(", uri: ").append(uri);
    return sb.toString();
  }

  private void setUri(String uriString) {
    if (uriString != null && uriString.length() > 0) {
      uri = uriString.split(Metadata.SEPARATOR);
    }
  }
}

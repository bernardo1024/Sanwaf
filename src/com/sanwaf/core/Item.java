package com.sanwaf.core;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

abstract class Item {
  static final String FAILED_PATTERN = "Failed Pattern: ";
  static final String INVALID_SIZE = "Invalid Size";
  static final String INVALID_URI = "Invalid URI";
  com.sanwaf.log.Logger logger;
  String name;
  String display;
  int max = Integer.MAX_VALUE;
  int min = 0;
  double maxValue;
  double minValue;
  String msg = null;
  String[] uri = null;
  Modes mode = Modes.BLOCK;
  boolean required = false;
  String related;
  String maskError = "";

  Item() {}

  Item(ItemData id) {
    name = id.name;
    mode = Shield.getMode(id.sMode, Modes.BLOCK);
    if(id.display.length() == 0) {
      display = name;
    }
    else {
      display = id.display;
    }
    max = id.max;
    min = id.min;
    msg = id.msg;
    setUri(id.uri);
  }

  //to be implemented by Types
  abstract boolean inError(ServletRequest req, Shield shield, String value);
  abstract List<Point> getErrorPoints(Shield shield, String value);
  abstract Types getType();

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
    if (!required && (value == null || value.length() == 0)) {
      return false;
    } 
    return (value.length() < min || value.length() > max);
  }

  String modifyErrorMsg(ServletRequest req, String errorMsg) {
    return errorMsg;
  }

  private void setUri(String uriString) {
    if (uriString != null && uriString.length() > 0) {
      uri = uriString.split(Shield.SEPARATOR);
    }
  }

  boolean handleMode(boolean ret, String value, String more){
    if(mode == Modes.BLOCK) {
      return ret;
    }
    if(ret && (mode == Modes.DETECT || mode == Modes.DETECT_ALL)) {
      logger.info(toString(value, more));
    }
    return false;
  }
  
  public String toString() {
    return toString(null, null);
  }
  public String toString(String value, String more) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"mode\":\"").append(mode).append("\"");
    sb.append(",\"name\":\"").append(Metadata.jsonEncode(name)).append("\"");
    sb.append(",\"type\":\"").append(getType()).append("\"");
    sb.append(",\"max\":\"").append(max).append("\"");
    sb.append(",\"min\":\"").append(min).append("\"");
    sb.append(",\"msg\":\"").append(Metadata.jsonEncode(msg)).append("\"");
    sb.append(",\"uri\":\"").append(Metadata.jsonEncode(String.valueOf(uri))).append("\"");
    sb.append(",\"required\":\"").append(required).append("\"");
    sb.append(",\"max-value\":\"").append(maxValue).append("\"");
    sb.append(",\"min-value\":\"").append(minValue).append("\"");
    sb.append(",\"mask-err\":\"").append(Metadata.jsonEncode(maskError)).append("\"");
    sb.append(",\"related\":\"").append(Metadata.jsonEncode(related)).append("\"");
    if(value != null && value.length() > 0) {
      sb.append(",\"value\":\"").append(Metadata.jsonEncode(value)).append("\"");
    }
    if(more != null && more.length() > 0) {
      sb.append(",\"more\":\"").append(Metadata.jsonEncode(more)).append("\"");
    }
    sb.append("}");
    return sb.toString();
  }
 
}
class ItemData{
  String name;
  String display;
  String type;
  int min;
  int max;
  String msg;
  String uri;
  String sMode;
  
  ItemData(String name, String sMode, String display, String type, String msg, String uri, int max, int min){
    this.name = name; 
    this.display = display; 
    this.type = type;
    this.min = min;
    this.max = max; 
    this.msg = msg;
    this.uri = uri;
    this.sMode = sMode;
  } 
}

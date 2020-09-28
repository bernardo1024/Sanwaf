package com.sanwaf.core;

import java.util.List;

import javax.servlet.ServletRequest;

abstract class Parameter {
  String name;
  String type = null;
  int max = Integer.MAX_VALUE;
  int min = 0;
  String errorMsg = null;
  String path = null;

  Parameter() {
  }

  Parameter(String name, int max, int min, String errorMsg, String path) {
    this.name = name;
    this.max = max;
    this.min = min;
    this.errorMsg = errorMsg;
    this.path = path;
  }

  public abstract boolean inError(ServletRequest req, Shield shield, String value);

  public abstract List<Point> getErrorPoints(Shield shield, String value);

  boolean isSizeError(String value) {
    return (value.length() < min || value.length() > max);
  }

  public String modifyErrorMsg(String errorMsg) {
    return errorMsg;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("type: ").append(type);
    sb.append(", max: ").append(max);
    sb.append(", min: ").append(min);
    sb.append(", path: ").append(path);
    sb.append(", error-msg: ").append(errorMsg);
    sb.append(", path: ").append(path);
    return sb.toString();
  }

}

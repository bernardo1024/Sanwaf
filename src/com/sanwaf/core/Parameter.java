package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

abstract class Parameter {
  String name;
  String type = null;
  int max = Integer.MAX_VALUE;
  int min = 0;
  String msg = null;
  String uri = null;

  Parameter() {
  }

  Parameter(String name, int max, int min, String msg, String uri) {
    this.name = name;
    this.max = max;
    this.min = min;
    this.msg = msg;
    this.uri = uri;
  }

  abstract boolean inError(ServletRequest req, Shield shield, String value);

  List<Point> getErrorPoints(Shield shield, String value){
    List<Point> points = new ArrayList<>();
    points.add(new Point(0, value.length()));
    return points;
  }

  boolean isPathValid(ServletRequest req) {
    if(uri == null || uri.length() == 0) { return true; }
    String reqUri = ((HttpServletRequest)req).getRequestURI();
    if(reqUri == null || reqUri.length() == 0) { return true; }
    
    return uri.equals(reqUri);
  }
  
  boolean isSizeError(String value) {
    if(value == null) { 
      return  min > 0;
    }
    else {  
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

}

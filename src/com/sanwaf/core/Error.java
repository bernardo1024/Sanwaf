package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;

final class Error {
  static final String ARRAY_START = "[";
  static final String ARRAY_END = "]";
  static final String OBJ_START = "{";
  static final String OBJ_END = "}";
  static final String OBJ_SEP = ",";
  static final String QUOTE = "\"";
  static final String K_V_SEP = ":";

  String shieldName;
  String key;
  String value;
  List<Point> errorPoints = new ArrayList<>();
  String message;
  String typeString;
  String appVersion = Sanwaf.securedAppVersion;

  Error(Shield shield, Datatype type, String key, String value, int min, int max) {
    this.shieldName = shield.name;
    this.key = key;
    this.value = value;
    this.typeString = type.name();
    this.errorPoints.addAll(type.getErrorHighlightPoints(shield, key, value));
    this.message = Datatype.getErrorMessage(shield, key, type);
    int l = value.length();
    if (l < min || l > max) {
      this.message += "<br>Invalid length. Must be between " + min + " and " + max + " characters";
    }
  }

  String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append(OBJ_START);
    sb.append(QUOTE).append("key").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(key).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("value").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Util.jsonEncode(value)).append(QUOTE);

    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("samplePoints").append(QUOTE).append(K_V_SEP);

    sb.append(ARRAY_START);
    boolean doneFirst = false;
    for (Point p : errorPoints) {
      if (doneFirst) {
        sb.append(OBJ_SEP);
      } else {
        doneFirst = true;
      }
      sb.append(OBJ_START);
      sb.append(QUOTE).append("start").append(QUOTE).append(K_V_SEP);
      sb.append(QUOTE).append(p.start).append(QUOTE);
      sb.append(OBJ_SEP);
      sb.append(QUOTE).append("end").append(QUOTE).append(K_V_SEP);
      sb.append(QUOTE).append(p.end).append(QUOTE);
      sb.append(OBJ_END);

    }
    sb.append(ARRAY_END);

    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("error").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Util.jsonEncode(message)).append(QUOTE);

    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("type").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(typeString).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("shieldName").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Util.jsonEncode(shieldName)).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("appVersion").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Util.jsonEncode(appVersion)).append(QUOTE);
    sb.append(OBJ_END);
    return sb.toString();
  }

  static String toJson(List<Error> errors) {
    if (errors == null || errors.isEmpty()) {
      return ARRAY_START + ARRAY_END;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(ARRAY_START);
    boolean isFirst = true;
    for (Error error : errors) {
      if (!isFirst) {
        sb.append(OBJ_SEP);
      } else {
        isFirst = false;
      }
      sb.append(error.toJson());
    }
    sb.append(ARRAY_END);
    return sb.toString();
  }

  void addPoint(Point point) {
    errorPoints.add(point);
  }
}

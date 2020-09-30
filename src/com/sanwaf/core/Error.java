package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Error {
  String shieldName;
  String key;
  String value;
  List<Point> errorPoints = new ArrayList<>();
  String message;
  String typeString;
  String appVersion = Sanwaf.securedAppVersion;

  Error(Shield shield, Item p, String key, String value) {
    this.shieldName = shield.name;
    this.key = key;
    this.value = value;
    this.typeString = p.type;
    this.message = getErrorMessage(shield, p);

    if (value != null) {
      this.errorPoints.addAll(p.getErrorPoints(shield, value));
      if (value.length() < p.min || value.length() > p.max) {
        this.message += "<br>Invalid length. Must be between " + p.min + " and " + p.max + " characters";
      }
    }
  }

  static String getErrorMessage(final Shield shield, final Item p) {
    String err = null;
    if (p.msg != null && p.msg.length() > 0) {
      err = p.msg;
    } else {
      err = shield.errorMessages.get(p.type);
      if (err == null || err.length() == 0) {
        err = shield.sanwaf.globalErrorMessages.get(p.type);
      }
    }
    return p.modifyErrorMsg(err);
  }

  static final String ARRAY_START = "[";
  static final String ARRAY_END = "]";
  static final String OBJ_START = "{";
  static final String OBJ_END = "}";
  static final String OBJ_SEP = ",";
  static final String QUOTE = "\"";
  static final String K_V_SEP = ":";

  static String toJson(List<Error> errors) {
    if (errors == null || errors.isEmpty()) { return ARRAY_START + ARRAY_END; }
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

  String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append(OBJ_START);
    sb.append(QUOTE).append("key").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(key).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("value").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Metadata.jsonEncode(value)).append(QUOTE);

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
    sb.append(QUOTE).append(Metadata.jsonEncode(message)).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("type").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(typeString).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("shieldName").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Metadata.jsonEncode(shieldName)).append(QUOTE);
    sb.append(OBJ_SEP);
    sb.append(QUOTE).append("appVersion").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Metadata.jsonEncode(appVersion)).append(QUOTE);
    sb.append(OBJ_END);
    return sb.toString();
  }

  static final String XML_ERROR_MSG = "errorMessages";
  static final String XML_ERROR_MSG_ALHPANUMERIC = "alphanumeric";
  static final String XML_ERROR_MSG_ALPHANUMERIC_AND_MORE = "alphanumericAndMore";
  static final String XML_ERROR_MSG_CHAR = "char";
  static final String XML_ERROR_MSG_NUMERIC = "numeric";
  static final String XML_ERROR_MSG_NUMERIC_DELIMITED = "numericDelimited";
  static final String XML_ERROR_MSG_STRING = "string";
  static final String XML_ERROR_MSG_REGEX = "regex";
  static final String XML_ERROR_MSG_JAVA = "java";
  static final String XML_ERROR_MSG_CONSTANT = "constant";
  static final String XML_ERROR_MSG_PLACEHOLDER = "{0}";

  static void setErrorMessages(Map<String, String> map, Xml xmlString) {
    Xml xml = new Xml(xmlString.get(XML_ERROR_MSG));
    map.put(Item.ALPHANUMERIC, xml.get(XML_ERROR_MSG_ALHPANUMERIC));
    map.put(Item.ALPHANUMERIC_AND_MORE, xml.get(XML_ERROR_MSG_ALPHANUMERIC_AND_MORE));
    map.put(Item.CHAR, xml.get(XML_ERROR_MSG_CHAR));
    map.put(Item.NUMERIC, xml.get(XML_ERROR_MSG_NUMERIC));
    map.put(Item.NUMERIC_DELIMITED, xml.get(XML_ERROR_MSG_NUMERIC_DELIMITED));
    map.put(Item.STRING, xml.get(XML_ERROR_MSG_STRING));
    map.put(Item.REGEX, xml.get(XML_ERROR_MSG_REGEX));
    map.put(Item.JAVA, xml.get(XML_ERROR_MSG_JAVA));
    map.put(Item.CONSTANT, xml.get(XML_ERROR_MSG_CONSTANT));
  }
}

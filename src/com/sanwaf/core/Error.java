package com.sanwaf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Error {
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
  static final String XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER = "{0}";

  static Map<String, String> defaultErrorMessages = new HashMap<>();
  static Map<String, Map<String, String>> shieldErrorMessages = new HashMap<>();

  String shieldName;
  String key;
  String value;
  List<Point> errorPoints = new ArrayList<>();
  String message;
  String typeString;
  String appVersion = Sanwaf.securedAppVersion;

  Error(Shield shield, Parameter p, String key, String value) {
    this.shieldName = shield.name;
    this.key = key;
    this.value = value;
    this.typeString = p.name;
    this.message = getErrorMessage(shield, p);

    if (value != null) {
      this.errorPoints.addAll(p.getErrorHighlightPoints(shield, value));
      if (value.length() < p.min || value.length() > p.max) {
        this.message += "<br>Invalid length. Must be between " + p.min + " and " + p.max + " characters";
      }
    }
  }

  void addPoint(Point point) {
    errorPoints.add(point);
  }

  static void setDefaultErrorMessages(Xml xml) {
    Xml msgBlockXml = new Xml(xml.get(XML_ERROR_MSG));
    defaultErrorMessages.put(Metadata.TYPE_ALPHANUMERIC, msgBlockXml.get(XML_ERROR_MSG_ALHPANUMERIC));
    defaultErrorMessages.put(Metadata.TYPE_ALPHANUMERIC_AND_MORE, msgBlockXml.get(XML_ERROR_MSG_ALPHANUMERIC_AND_MORE));
    defaultErrorMessages.put(Metadata.TYPE_CHAR, msgBlockXml.get(XML_ERROR_MSG_CHAR));
    defaultErrorMessages.put(Metadata.TYPE_NUMERIC, msgBlockXml.get(XML_ERROR_MSG_NUMERIC));
    defaultErrorMessages.put(Metadata.TYPE_NUMERIC_DELIMITED, msgBlockXml.get(XML_ERROR_MSG_NUMERIC_DELIMITED));
    defaultErrorMessages.put(Metadata.TYPE_STRING, msgBlockXml.get(XML_ERROR_MSG_STRING));
    defaultErrorMessages.put(Metadata.TYPE_REGEX, msgBlockXml.get(XML_ERROR_MSG_REGEX));
    defaultErrorMessages.put(Metadata.TYPE_JAVA, msgBlockXml.get(XML_ERROR_MSG_JAVA));
    defaultErrorMessages.put(Metadata.TYPE_CONSTANT, msgBlockXml.get(XML_ERROR_MSG_CONSTANT));
  }

  static void setShieldErrorMessages(Xml xml, String shieldName) {
    String msgBlock = xml.get(XML_ERROR_MSG);
    Xml msgBlockXml = new Xml(msgBlock);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_ALHPANUMERIC, Metadata.TYPE_ALPHANUMERIC, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_ALPHANUMERIC_AND_MORE, Metadata.TYPE_ALPHANUMERIC_AND_MORE, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_CHAR, Metadata.TYPE_CHAR, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_NUMERIC, Metadata.TYPE_NUMERIC, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_NUMERIC_DELIMITED, Metadata.TYPE_NUMERIC_DELIMITED, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_STRING, Metadata.TYPE_STRING, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_REGEX, Metadata.TYPE_REGEX, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_JAVA, Metadata.TYPE_JAVA, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_CONSTANT, Metadata.TYPE_CONSTANT, shieldName);
  }

  private static void putShieldErrorMessage(Xml xml, String key, String type, String shieldName) {
    String s = xml.get(key);
    if (s.length() > 0) {
      Map<String, String> map = shieldErrorMessages.get(shieldName);
      if (map == null) {
        map = new HashMap<>();
      }
      map.put(type, s);
      shieldErrorMessages.put(shieldName, map);
    }
  }

  static String getErrorMessage(final Shield shield, final Parameter p) {
    String err = null;
    if (p.errorMsg != null && p.errorMsg.length() > 0) {
      err = p.errorMsg;
    } else {
      Map<String, String> m = shieldErrorMessages.get(shield.name);
      if (m != null) {
        err = m.get(p.type);
      } else {
        err = defaultErrorMessages.get(p.type);
      }
    }

    if (p.type.equals(Metadata.TYPE_ALPHANUMERIC_AND_MORE)) {
      return ((ParameterAlphanumericAndMore) (p)).substituteAlphaNumericAndMoreChars(err);
    } else if (p.type.equals(Metadata.TYPE_NUMERIC_DELIMITED)) {
      return ((ParameterNumericDelimited) (p)).substituteNumericDelimiter(err);
    } else if (p.type.equals(Metadata.TYPE_CONSTANT)) {
      return ((ParameterConstant) (p)).substituteConstantValues(err);
    }
    return err;
  }

  static final String ARRAY_START = "[";
  static final String ARRAY_END = "]";
  static final String OBJ_START = "{";
  static final String OBJ_END = "}";
  static final String OBJ_SEP = ",";
  static final String QUOTE = "\"";
  static final String K_V_SEP = ":";

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

}


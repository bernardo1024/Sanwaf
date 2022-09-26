package com.sanwaf.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

final class Error {
  String shieldName;
  String key;
  String display;
  String value;
  List<Point> errorPoints = new ArrayList<>();
  String message;
  String typeString;
  String appVersion = Sanwaf.securedAppVersion;

  Error(ServletRequest req, Shield shield, Item p, String key, String value) {
    this.shieldName = shield.name;
    this.key = key;
    if(p.display == null || p.display.length() == 0) {
      this.display = key;
    }
    else {
      this.display = p.display;
    }
    if(p.maskError.length() > 0) {
      value = p.maskError;
    }
    this.value = value;
    this.typeString = p.getType().toString();
    this.message = getErrorMessage(req, shield, p);

    if (value != null) {
      this.errorPoints.addAll(p.getErrorPoints(shield, value));
      if(p.required && value.length() == 0) {
        this.message += getErrorMessage(req, shield, p, XML_REQUIRED_MSG);
      }
      if (value.length() < p.min || value.length() > p.max) {
        this.message += modifyInvalidLengthErrorMsg(getErrorMessage(req, shield, p, XML_INVALID_LENGTH_MSG), p.min, p.max);
      }
    }
  }

  static String getErrorMessage(final ServletRequest req, final Shield shield, final Item p) {
    return   getErrorMessage(req, shield, p, null);
  }
  
  static String getErrorMessage(final ServletRequest req, final Shield shield, final Item p, String errorMsgKey) {
    String err = null;
    if(errorMsgKey == null && p.msg != null && p.msg.length() > 0) {
        err = p.msg;
    }
    if(err == null) {
      if(errorMsgKey == null) {
        errorMsgKey = p.getType().toString();
      }
      err = shield.errorMessages.get(errorMsgKey);
      if (err == null || err.length() == 0) {
        err = shield.sanwaf.globalErrorMessages.get(errorMsgKey);
      }
    }
    return p.modifyErrorMsg(req, err);
  }

  static String modifyInvalidLengthErrorMsg(String errorMsg, int min, int max ) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      errorMsg = errorMsg.substring(0, i) + min + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER2);
    if (i >= 0) {
      errorMsg = errorMsg.substring(0, i) + max + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
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
    sb.append(QUOTE).append("display").append(QUOTE).append(K_V_SEP);
    sb.append(QUOTE).append(Metadata.jsonEncode(display)).append(QUOTE);
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
  static final String XML_ERROR_MSG_INTEGER = "integer";
  static final String XML_ERROR_MSG_INTEGER_DELIMITED = "integerDelimited";
  static final String XML_ERROR_MSG_STRING = "string";
  static final String XML_ERROR_MSG_OPEN = "open";
  static final String XML_ERROR_MSG_REGEX = "regex";
  static final String XML_ERROR_MSG_JAVA = "java";
  static final String XML_ERROR_MSG_CONSTANT = "constant";
  static final String XML_ERROR_MSG_FORMAT = "format";
  static final String XML_ERROR_MSG_DEPENDENT_FORMAT = "dependentFormat";
  static final String XML_INVALID_LENGTH_MSG = "invalidLength";
  static final String XML_REQUIRED_MSG = "required";
  static final String XML_ERROR_MSG_PLACEHOLDER1 = "{0}";
  static final String XML_ERROR_MSG_PLACEHOLDER2 = "{1}";

  static void setErrorMessages(Map<String, String> map, Xml xmlString) {
    Xml xml = new Xml(xmlString.get(XML_ERROR_MSG));
    map.put(String.valueOf(Types.ALPHANUMERIC), xml.get(XML_ERROR_MSG_ALHPANUMERIC));
    map.put(String.valueOf(Types.ALPHANUMERIC_AND_MORE), xml.get(XML_ERROR_MSG_ALPHANUMERIC_AND_MORE));
    map.put(String.valueOf(Types.CHAR), xml.get(XML_ERROR_MSG_CHAR));
    map.put(String.valueOf(Types.NUMERIC), xml.get(XML_ERROR_MSG_NUMERIC));
    map.put(String.valueOf(Types.NUMERIC_DELIMITED), xml.get(XML_ERROR_MSG_NUMERIC_DELIMITED));
    map.put(String.valueOf(Types.INTEGER), xml.get(XML_ERROR_MSG_INTEGER));
    map.put(String.valueOf(Types.INTEGER_DELIMITED), xml.get(XML_ERROR_MSG_INTEGER_DELIMITED));
    map.put(String.valueOf(Types.STRING), xml.get(XML_ERROR_MSG_STRING));
    map.put(String.valueOf(Types.OPEN), xml.get(XML_ERROR_MSG_OPEN));
    map.put(String.valueOf(Types.REGEX), xml.get(XML_ERROR_MSG_REGEX));
    map.put(String.valueOf(Types.JAVA), xml.get(XML_ERROR_MSG_JAVA));
    map.put(String.valueOf(Types.CONSTANT), xml.get(XML_ERROR_MSG_CONSTANT));
    map.put(String.valueOf(Types.FORMAT), xml.get(XML_ERROR_MSG_FORMAT));
    map.put(String.valueOf(Types.DEPENDENT_FORMAT), xml.get(XML_ERROR_MSG_DEPENDENT_FORMAT));
    map.put(XML_INVALID_LENGTH_MSG, xml.get(XML_INVALID_LENGTH_MSG));
    map.put(XML_REQUIRED_MSG, xml.get(XML_REQUIRED_MSG));
  }
}

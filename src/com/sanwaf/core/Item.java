package com.sanwaf.core;

import java.util.ArrayList;
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
  Shield shield;
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
    mode = id.mode;
    shield = id.shield;
    if(shield != null) {
      logger = id.shield.logger;
    }
    
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

  //implemented by Types
  abstract boolean inError(ServletRequest req, Shield shield, String value);
  abstract List<Point> getErrorPoints(Shield shield, String value);
  abstract Types getType();

  //evaluate the mode, URI & size. The method returns null if no definitive results was found and caller continues validation
  ModeError isModeError(ServletRequest req, String value) {
    ModeError me = new ModeError();
    if (mode == Modes.DISABLED) { 
      me.error = false; 
    } else if (!isUriValid(req)) {
      me.error = true;
    } else if (isSizeError(value)) {
      me.error = true;
    } else if(value != null && value.length() == 0) {
      me.error = false;
    } else {
      return null;
    }
    return me;
  }
  
  boolean isUriValid(ServletRequest req) {
    if (uri == null || req == null) {
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
  
  boolean handleMode(boolean isError, String value, String more, ServletRequest req){
    return handleMode(isError, value, more, req, true);
  }

  boolean handleMode(boolean isError, String value, String more, ServletRequest req, boolean log){
    if(isError && mode == Modes.BLOCK) {
      if(logger != null && log && (shield == null || shield.sanwaf.onErrorLogParmErrors)) {
        logger.error(toJson(value, more, req, true));
      }
      if(log && req != null && (shield == null || shield.sanwaf.onErrorAddParmErrors)) {
        appendAttribute(Sanwaf.ATT_LOG_ERROR, toJson(value, more, req, true), req);
      }
      return isError;
    }
    if(isError && (mode == Modes.DETECT || mode == Modes.DETECT_ALL)) {
      if(logger != null && log && (shield == null || shield.sanwaf.onErrorLogParmDetections)) {
        logger.warn(toJson(value, more, req, true));
      }
      if(log && req != null && (shield == null || shield.sanwaf.onErrorAddParmDetections)) {
        appendAttribute(Sanwaf.ATT_LOG_DETECT, toJson(value, more, req, true), req);
      }
    }
    return false;
  }
  
  void appendAttribute(String att, String value, ServletRequest req) {
    if(req == null) { return; }
      String old = (String)req.getAttribute(att);
      if(old == null || old.length() < 2) { 
        old = ""; 
      }
      else {
        old = old.substring(1, old.length()-1) + ",";
      } 
      req.setAttribute(att, "[" + old + value + "]");
  }
  
  String getProperties() {
    return null;
  }
  
  public String toString() {
    return toJson(null, null, null, true);
  }

  public String toJson(String value, String more, ServletRequest req, boolean verbose) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    
    if(req != null) {
      HttpServletRequest hreq = (HttpServletRequest)req;
      sb.append("\"transid\":\"").append(hreq.getAttribute(Metadata.jsonEncode(Sanwaf.ATT_TRANS_ID))).append("\"");
      sb.append(",\"ip\":\"").append(hreq.getRemoteAddr()).append("\"");
      sb.append(",\"referer\":\"").append(Metadata.jsonEncode(hreq.getHeader("referer"))).append("\",");
    }
    
    if(shield != null && verbose) {
      sb.append("\"shield\":{\"name\":\"").append(shield.name).append("\"");
      sb.append(",\"mode\":\"").append(shield.mode).append("\"");
      sb.append(",\"appversion\":\"").append(Sanwaf.securedAppVersion).append("\"");
      sb.append("},");
    }
    
    sb.append("\"item\":{\"name\":\"").append(Metadata.jsonEncode(name)).append("\"");
    sb.append(",\"display\":\"").append(Metadata.jsonEncode(display)).append("\"");
    sb.append(",\"mode\":\"").append(mode).append("\"");
    sb.append(",\"type\":\"").append(getType()).append("\"");
    
    if(value != null && value.length() > 0) {
      sb.append(",\"value\":\"");
      String mValue = value;
      if(maskError.length() > 0) {
        mValue = maskError;
      }
      sb.append(Metadata.jsonEncode(mValue.length() < 100 ? mValue : (mValue.substring(0, 100) + "..."))).append("\"");
    }
    else {
      sb.append(",\"value\":\"").append(value).append("\"");
    }
    
    if(more != null && more.length() > 0) {
        sb.append(",\"more\":\"").append(Metadata.jsonEncode(more)).append("\"");
    }

    if(shield != null) {
      String errMsg = getErrorMessage(req, shield, this);
      if(required && value.length() == 0) {
        errMsg += getErrorMessage(req, shield, this, ItemFactory.XML_REQUIRED_MSG);
      }
      if (value != null && (value.length() < min || value.length() > max)) {
        errMsg += modifyInvalidLengthErrorMsg(getErrorMessage(req, shield, this, ItemFactory.XML_INVALID_LENGTH_MSG), min, max);
      }
      sb.append(",\"error\":\"").append(Metadata.jsonEncode(errMsg)).append("\"");
    }
    
    if(value != null && shield != null && verbose) {
      List<Point> errorPoints = new ArrayList<>();
      errorPoints.addAll(getErrorPoints(shield, value));
      sb.append(",\"samplePoints\":[");
      boolean doneFirst = false;
      for (Point p : errorPoints) {
        if (doneFirst) {
          sb.append(",");
        } else {
          doneFirst = true;
        }
        sb.append("{\"start\":\"").append(p.start).append("\"");
        sb.append(",\"end\":\"").append(p.end).append("\"}");
      }
      sb.append("]");
    }

    if(shield != null && verbose) {
      sb.append(",\"properties\": {");
      sb.append("\"maxlength\":\"").append(max).append("\"");
      sb.append(",\"minlength\":\"").append(min).append("\"");
      sb.append(",\"msg\":\"").append(Metadata.jsonEncode(msg)).append("\"");
      sb.append(",\"uri\":\"").append(Metadata.jsonEncode(String.valueOf(uri))).append("\"");
      sb.append(",\"req\":\"").append(required).append("\"");
      sb.append(",\"maxvalue\":\"").append(maxValue).append("\"");
      sb.append(",\"minvalue\":\"").append(minValue).append("\"");
      sb.append(",\"maskerr\":\"").append(Metadata.jsonEncode(maskError)).append("\"");
      sb.append(",\"related\":\"").append(Metadata.jsonEncode(related)).append("\"");
      String s = getProperties();
      if(s != null && s.length() > 0) {
        sb.append(",").append(s);
      }
      sb.append("}}");
    }
    sb.append("}");
    return sb.toString();
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
    int i = errorMsg.indexOf(ItemFactory.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      errorMsg = errorMsg.substring(0, i) + min + errorMsg.substring(i + ItemFactory.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    i = errorMsg.indexOf(ItemFactory.XML_ERROR_MSG_PLACEHOLDER2);
    if (i >= 0) {
      errorMsg = errorMsg.substring(0, i) + max + errorMsg.substring(i + ItemFactory.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }
}

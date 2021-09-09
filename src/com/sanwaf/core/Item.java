package com.sanwaf.core;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

abstract class Item {
  static final String INTEGER = "i";
  static final String INTEGER_DELIMITED = "i{";
  static final String NUMERIC = "n";
  static final String NUMERIC_DELIMITED = "n{";
  static final String ALPHANUMERIC = "a";
  static final String ALPHANUMERIC_AND_MORE = "a{";
  static final String STRING = "s";
  static final String OPEN = "o";
  static final String CHAR = "c";
  static final String REGEX = "r{";
  static final String INLINE_REGEX = "x{";
  static final String JAVA = "j{";
  static final String CONSTANT = "k{";
  static final String FORMAT = "f{";
  static final String DEPENDENT_FORMAT = "d{";

  static final String INVALID_LENGTH_MSG = "length_msg";
  static final String REQUIRED_MSG = "r_msg";
  
  static final String SEP_START = "{";
  static final String SEP_END = "}";

  static final String XML_ITEMS = "items";
  static final String XML_ITEM = "item";
  static final String XML_ITEM_NAME = "name";
  static final String XML_ITEM_TYPE = "type";
  static final String XML_ITEM_MAX = "max";
  static final String XML_ITEM_MIN = "min";
  static final String XML_ITEM_MSG = "msg";
  static final String XML_ITEM_URI = "uri";
  static final String XML_ITEM_REQUIRED = "req";
  static final String XML_ITEM_MAX_VAL = "max-value";
  static final String XML_ITEM_MIN_VAL = "min-value";
  static final String XML_ITEM_RELATED = "related";
  static final String XML_ITEM_MASK_ERROR = "mask-err";

  String name;
  String type = null;
  int max = Integer.MAX_VALUE;
  int min = 0;
  double maxValue;
  double minValue;
  String msg = null;
  String[] uri = null;

  boolean required = false;
  String related;
  String maskError = "";

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

  static Item parseItem(Xml xml) {
    return parseItem(xml, false);
  }

  static Item parseItem(Xml xml, boolean includeEnpointAttributes) {
    String name = xml.get(XML_ITEM_NAME);
    String type = xml.get(XML_ITEM_TYPE);
    String msg = xml.get(XML_ITEM_MSG);
    String uri = xml.get(XML_ITEM_URI);
    String sMax = xml.get(XML_ITEM_MAX);
    String sMin = xml.get(XML_ITEM_MIN);

    int max = Integer.MAX_VALUE;
    int min = 0;
    if (sMax.length() > 0) {
      max = Integer.parseInt(sMax);
    }
    if (sMin.length() > 0) {
      min = Integer.parseInt(sMin);
    }
    if (max == -1) {
      max = Integer.MAX_VALUE;
    }
    if (min == -1) {
      min = Integer.MAX_VALUE;
    }
    if (min < -1) {
      min = 0;
    }
    Item item = Item.getNewItem(name, type, min, max, msg, uri);

    item.required = Boolean.valueOf(xml.get(XML_ITEM_REQUIRED));

    item.maxValue = Integer.MIN_VALUE;
    String sMaxVal = xml.get(XML_ITEM_MAX_VAL);
    if (sMaxVal.length() > 0) {
      item.maxValue = Double.valueOf(sMaxVal);
    }

    item.minValue = Integer.MIN_VALUE;
    String sMinVal = xml.get(XML_ITEM_MIN_VAL);
    if (sMinVal.length() > 0) {
      item.minValue = Double.valueOf(sMinVal);
    }
    
    item.maskError = xml.get(XML_ITEM_MASK_ERROR);

    if (includeEnpointAttributes) {
      item.related = removeRelatedSpace(xml.get(XML_ITEM_RELATED));
    }
    if(item instanceof ItemDependentFormat) {
      ((ItemDependentFormat)item).setAdditionalFields();
    }
    return item;
  }

  private static String removeRelatedSpace(String related) {
    related = related.trim();
    related = related.replaceAll("\\)\\s+&&\\s+\\(", ")&&(");
    related = related.replaceAll("\\s+\\|\\|\\s+", "||");
    related = related.replaceAll("\\s+:\\s+", ":");
    related = related.replaceAll("\\(\\s+", "(");
    related = related.replaceAll("\\s+\\)", ")");
    return related;
  }

  static Item getNewItem(String name, Item item) {
    item.name = name;
    return item;
  }

  static Item getNewItem(String name, String type, int min, int max, String msg, String uri) {
    Item item = null;
    String t = type.toLowerCase();
    int pos = t.indexOf(SEP_START);
    if (pos > 0) {
      t = t.substring(0, pos + SEP_START.length());
    }

    if (t.equals(NUMERIC)) {
      item = new ItemNumeric(name, max, min, msg, uri,false);
    } else if (t.equals(NUMERIC_DELIMITED)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemNumericDelimited(name, type, max, min, msg, uri, false);
    } else if (t.equals(INTEGER)) {
      item = new ItemNumeric(name, max, min, msg, uri, true);
    } else if (t.equals(INTEGER_DELIMITED)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemNumericDelimited(name, type, max, min, msg, uri, true);
    } else if (t.equals(ALPHANUMERIC)) {
      item = new ItemAlphanumeric(name, max, min, msg, uri);
    } else if (t.equals(ALPHANUMERIC_AND_MORE)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemAlphanumericAndMore(name, type, max, min, msg, uri);
    } else if (t.equals(CHAR)) {
      item = new ItemChar(name, max, min, msg, uri);
    } else if (t.equals(REGEX) || t.equals(INLINE_REGEX)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemRegex(name, type, max, min, msg, uri);
    } else if (t.equals(JAVA)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemJava(name, type, max, min, msg, uri);
    } else if (t.equals(CONSTANT)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemConstant(name, type, max, min, msg, uri);
    } else if (t.equals(FORMAT)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemFormat(name, type, max, min, msg, uri);
    } else if (t.equals(DEPENDENT_FORMAT)) {
      type = ensureComplexTypeFormat(type);
      item = new ItemDependentFormat(name, type, max, min, msg, uri);
    } else if (t.equals(OPEN)) {
      item = new ItemOpen(name, max, min, msg, uri);
    } else {
      item = new ItemString(name, max, min, msg, uri);
    }
    return item;
  }

  private static String ensureComplexTypeFormat(String type) {
    if (!type.endsWith(SEP_END)) {
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

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("name: ").append(name);
    sb.append(", type: ").append(type);
    sb.append(", max: ").append(max);
    sb.append(", min: ").append(min);
    sb.append(", msg: ").append(msg);
    sb.append(", uri: ").append(uri);
    sb.append(", required: ").append(required);
    sb.append(", max-value: ").append(maxValue);
    sb.append(", min-value: ").append(minValue);
    sb.append(", mask-err: ").append(maskError);
    sb.append(", related: ").append(related);
    return sb.toString();
  }
}

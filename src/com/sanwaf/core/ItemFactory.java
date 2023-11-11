package com.sanwaf.core;

import java.util.Map;

public class ItemFactory {
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

  static final String XML_ITEMS = "items";
  static final String XML_ITEM = "item";
  static final String XML_ITEM_NAME = "name";
  static final String XML_ITEM_MODE = "mode";
  static final String XML_ITEM_MATCH = "match";
  static final String XML_ITEM_DISPLAY = "display";
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

  ItemFactory() {
    // no instances allowed
  }

  static Item parseItem(Shield shield, Xml xml, com.sanwaf.log.Logger logger) {
    // TODO: Cache the dynamic creation of Items - hash regex as key and look up
    // in table. if found, return, else build new
    return parseItem(shield, xml, false, logger);
  }

  static Item parseItem(Shield shield, Xml xml, boolean includeEnpointAttributes, com.sanwaf.log.Logger logger) {
    String name = xml.get(XML_ITEM_NAME);
    Modes mode = Modes.getMode(xml.get(XML_ITEM_MODE), (shield != null ? shield.mode : Modes.BLOCK));
    String display = xml.get(XML_ITEM_DISPLAY);
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
      min = Integer.MIN_VALUE;
    }
    if (min < -1) {
      min = 0;
    }
    Item item = getNewItem(new ItemData(shield, name, mode, display, type, msg, uri, max, min));
    item.logger = logger;
    item.required = Boolean.valueOf(xml.get(XML_ITEM_REQUIRED));

    item.maxValue = Integer.MAX_VALUE;
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
    if (item instanceof ItemDependentFormat) {
      ((ItemDependentFormat) item).setAdditionalFields();
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

  static Item getNewItem(ItemData id) {
    String t = id.type.toLowerCase();
    int pos = t.indexOf(ItemFactory.SEP_START);
    if (pos > 0) {
      t = t.substring(0, pos + ItemFactory.SEP_START.length());
    }
    if (t.equals(NUMERIC)) {
      return new ItemNumeric(id, false);
    } else if (t.equals(OPEN)) {
      return new ItemOpen(id);
    } else if (t.equals(INTEGER)) {
      return new ItemNumeric(id, true);
    } else if (t.equals(ALPHANUMERIC)) {
      return new ItemAlphanumeric(id);
    } else if (t.equals(CHAR)) {
      return new ItemChar(id);
    }

    id.type = ensureComplexTypeFormat(id.type);

    if (t.equals(NUMERIC_DELIMITED)) {
      return new ItemNumericDelimited(id, false);
    } else if (t.equals(INTEGER_DELIMITED)) {
      return new ItemNumericDelimited(id, true);
    } else if (t.equals(ALPHANUMERIC_AND_MORE)) {
      return new ItemAlphanumericAndMore(id);
    } else if (t.equals(REGEX) || t.equals(INLINE_REGEX)) {
      return new ItemRegex(id);
    } else if (t.equals(JAVA)) {
      return new ItemJava(id);
    } else if (t.equals(CONSTANT)) {
      return new ItemConstant(id);
    } else if (t.equals(FORMAT)) {
      return new ItemFormat(id);
    } else if (t.equals(DEPENDENT_FORMAT)) {
      return new ItemDependentFormat(id);
    }
    return new ItemString(id);
  }

  private static String ensureComplexTypeFormat(String type) {
    if (!type.endsWith(ItemFactory.SEP_END)) {
      return type + ItemFactory.SEP_END;
    }
    return type;
  }

  static final String SEP_START = "{";
  static final String SEP_END = "}";
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


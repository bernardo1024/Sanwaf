package com.sanwaf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Metadata {
  static final String INDEX_PARM_MARKER = "  ";
  static final String STAR = "*";
  static final String TYPE_NUMERIC = "n";
  static final String TYPE_NUMERIC_DELIMITED = "n{";
  static final String TYPE_ALPHANUMERIC = "a";
  static final String TYPE_ALPHANUMERIC_AND_MORE = "a{";
  static final String TYPE_STRING = "s";
  static final String TYPE_CHAR = "c";
  static final String TYPE_REGEX = "r{";
  static final String TYPE_JAVA = "j{";
  static final String TYPE_CONSTANT = "k{";

  static final String SEP_START_CHAR = "{";
  static final String SEP_END_CHAR = "}";
  static final String MSG_START_CHAR = "[";
  static final String MSG_END_CHAR = "]";
  static final String LENGTH_START_CHAR = "(";
  static final String LENGTH_MID_CHAR = ",";
  static final String LENGTH_END_CHAR = ")";

  static final String XML_METADATA = "metadata";
  static final String XML_CASE_SENSITIVE = "caseSensitive";
  static final String XML_SECURED = "secured";
  static final String XML_PARAMETERS = "parameters";
  static final String XML_HEADERS = "headers";
  static final String XML_COOKIES = "cookies";
  static final String XML_ENABLED = "enabled";

  boolean enabled = false;
  boolean caseSensitive = true;
  Map<String, Parameter> map = new HashMap<>();
  Map<String, List<String>> index = new HashMap<>();

  Metadata(Xml xml, String type) {
    load(xml, type);
  }

  String getFromIndex(String key) {
    List<String> list = index.get(key.substring(0, 1));
    if (list == null) {
      return null;
    }

    for (String s : list) {
      int last = 0;
      while (true) {
        if (s.length() != 2) {
          return resolveStarAtEndOfWord(key, list);
        }
        int start = key.indexOf(s.charAt(0), last);
        if (start <= 0) {
          break;
        }
        int end = key.indexOf(s.charAt(1), start + 1);
        last = end + 1;
        if (end > 0) {
          key = key.substring(0, start + 1) + key.substring(end, key.length());
        }
      }
    }
    return key;
  }

  private String resolveStarAtEndOfWord(String key, List<String> list) {
    String k2 = stripEosNumbers(key);
    if (list.contains(INDEX_PARM_MARKER + k2)) {
      return k2;
    }
    return null;
  }

  void load(Xml xml, String type) {
    initA2Zindex(index);

    String metadataBlock = xml.get(Metadata.XML_METADATA);
    Xml metadataBlockXml = new Xml(metadataBlock);
    String securedBlock = metadataBlockXml.get(XML_SECURED);
    Xml securedBlockXml = new Xml(securedBlock);

    String enabledViewBlock = metadataBlockXml.get(XML_ENABLED);
    Xml enabledViewdBlockXml = new Xml(enabledViewBlock);
    enabled = Boolean.parseBoolean(enabledViewdBlockXml.get(type));

    String caseBlock = metadataBlockXml.get(XML_CASE_SENSITIVE);
    Xml caseBlockXml = new Xml(caseBlock);
    caseSensitive = Boolean.parseBoolean(caseBlockXml.get(type));

    String subBlock = securedBlockXml.get(type);
    Xml subBlockXml = new Xml(subBlock);
    String[] items = subBlockXml.getAll(Shield.XML_ITEM);
    for (String item : items) {
      processItem(item);
    }
  }

  private void processItem(String item) {
    Xml xml = new Xml(item);
    String namesString = xml.get(Shield.XML_ITEM_NAME);
    String[] names = namesString.split(Shield.SPLIT_LINE_CHARS);

    String type = xml.get(Shield.XML_ITEM_TYPE);
    String msg = xml.get(Shield.XML_ITEM_MSG);
    String path = xml.get(Shield.XML_ITEM_PATH);
    String sMax = xml.get(Shield.XML_ITEM_MAX);
    String sMin = xml.get(Shield.XML_ITEM_MIN);

    int max = Integer.MAX_VALUE;
    int min = 0;
    if (sMax != null && sMax.length() > 0) {
      max = Integer.parseInt(sMax);
    }
    if (sMin != null && sMin.length() > 0) {
      min = Integer.parseInt(sMin);
    }
    if (max == -1) {
      max = Integer.MAX_VALUE;
    }
    if (min == -1) {
      min = Integer.MAX_VALUE;
    }

    for (String name : names) {
      name = refineName(name, index);
      if (name == null) {
        continue;
      }
      processType(name, type, min, max, msg, path);
    }
  }

  private void processType(String key, String value, int min, int max, String errorMsg, String path) {
    if (!caseSensitive) {
      key = key.toLowerCase();
    }
    Parameter p = null;
    String t = value.toLowerCase();
    int pos = t.indexOf(SEP_START_CHAR);
    if (pos > 0) {
      t = t.substring(0, pos + SEP_START_CHAR.length());
    }

    if (t.equals(TYPE_NUMERIC)) {
      p = new ParameterNumeric(key, max, min, errorMsg, path);
    } else if (t.equals(TYPE_NUMERIC_DELIMITED)) {
      p = new ParameterNumericDelimited(key, value, max, min, errorMsg, path);
    } else if (t.equals(TYPE_ALPHANUMERIC)) {
      p = new ParameterAlphanumeric(key, max, min, errorMsg, path);
    } else if (t.equals(TYPE_ALPHANUMERIC_AND_MORE)) {
      p = new ParameterAlphanumericAndMore(key, value, max, min, errorMsg, path);
    } else if (t.equals(TYPE_STRING)) {
      p = new ParameterString(key, max, min, errorMsg, path);
    } else if (t.equals(TYPE_CHAR)) {
      p = new ParameterChar(key, max, min, errorMsg, path);
    } else if (t.equals(TYPE_REGEX)) {
      p = new ParameterRegex(key, value, max, min, errorMsg, path);
    } else if (t.equals(TYPE_JAVA)) {
      p = new ParameterJava(key, value, max, min, errorMsg, path);
    } else if (t.equals(TYPE_CONSTANT)) {
      p = new ParameterConstant(key, value, max, min, errorMsg, path);
    }
    if (p != null) {
      map.put(key, p);
    }
  }

  static void initA2Zindex(Map<String, List<String>> map) {
    for (char ch = 'a'; ch <= 'z'; ++ch) {
      map.put(String.valueOf(ch), null);
    }
  }

  static String refineName(String name, Map<String, List<String>> map) {
    int last = 0;
    while (true) {
      int starPos = name.indexOf(STAR, last);
      if (starPos < 0) {
        return name;
      }
      if (starPos == 0) {
        return null;
      }
      String f = name.substring(starPos - 1, starPos);
      String markerChars;

      if (starPos == name.length() - 1) {
        markerChars = INDEX_PARM_MARKER + name.substring(0, name.length() - 1);
      } else {
        markerChars = f + name.substring(starPos + 1, starPos + 2);
        if (!isNotAlphanumeric(markerChars)) {
          return null;
        }
      }
      String firstCharOfKey = name.substring(0, 1);
      List<String> chars = map.get(firstCharOfKey);
      if (chars == null) {
        chars = new ArrayList<>();
        map.put(firstCharOfKey, chars);
      }
      if (!chars.contains(markerChars)) {
        chars.add(markerChars);
      }
      name = name.substring(0, starPos) + name.substring(starPos + 1, name.length());
    }
  }

  static String stripEosNumbers(final String s) {
    int i = s.length() - 1;
    while (i > 0) {
      char c = s.charAt(i);
      int v = c - '0';
      if (v >= 0 && v <= 9) {
        i--;
        continue;
      }
      return s.substring(0, i + 1);
    }
    return s;
  }

  static boolean isNotAlphanumeric(String s) {
    char[] chars = s.toCharArray();
    for (char c : chars) {
      if (!(c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a)) {
        return false;
      }
    }
    return true;
  }

  static String jsonEncode(String s) {
    if (s == null) {
      return "";
    } else {
      s = s.replace("\\", "\\\\");
      s = s.replace("\"", "\\\"");
      return s.replace("/", "\\/");
    }
  }
}


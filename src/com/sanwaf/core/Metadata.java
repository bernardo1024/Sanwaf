package com.sanwaf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Metadata {
  static final String XML_ITEM = "item";
  static final String XML_ITEM_NAME = "name";
  static final String XML_ITEM_TYPE = "type";
  static final String XML_ITEM_MAX = "max";
  static final String XML_ITEM_MIN = "min";
  static final String XML_ITEM_MSG = "msg";
  static final String XML_ITEM_URI = "uri";
  static final String XML_KEY = "key";
  static final String XML_VALUE = "value";
  static final String XML_NAME = "name";
  static final String XML_MIN_LEN = "minLen";
  static final String XML_MAX_LEN = "maxLen";
  static final String XML_CHILD = "child";
  static final String XML_CHILD_SHIELD = "child-shield";
  static final String XML_MAX_LEN_LOG = "maxLenLogViolation";
  static final String XML_MAX_LEN_FAIL = "maxLenFailOnViolation";
  static final String XML_REGEX = "regex";
  static final String XML_SHIELD_SETTINGS = "shield-settings";
  static final String XML_REGEX_CONFIG = "regex-config";
  static final String XML_REGEX_ALWAYS_REGEX = "alwaysPerformRegex";
  static final String XML_REGEX_ALWAYS_REGEX_EXCLUSIONS = "exclusions";
  static final String XML_REGEX_PATTERNS_AUTO = "autoRunPatterns";
  static final String XML_REGEX_PATTERNS_CUSTOM = "customPatterns";
  static final String XML_METADATA = "metadata";
  static final String XML_CASE_SENSITIVE = "caseSensitive";
  static final String XML_SECURED = "secured";
  static final String XML_PARAMETERS = "parameters";
  static final String XML_HEADERS = "headers";
  static final String XML_COOKIES = "cookies";
  static final String XML_ENABLED = "enabled";

  static final String INDEX_PARM_MARKER = "  ";
  static final String STAR = "*";
  static final String SEPARATOR = ":::";

  boolean enabled = false;
  boolean caseSensitive = true;
  Map<String, Item> items = new HashMap<>();
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
        key = key.substring(0, start + 1) + key.substring(end, key.length());
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

    String metadataBlock = xml.get(XML_METADATA);
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
    String[] xmlItems = subBlockXml.getAll(XML_ITEM);
    for (String itemString : xmlItems) {
      loadItem(itemString);
    }
  }

  private void loadItem(String itemString) {
    Xml xml = new Xml(itemString);
    Item item = parseItem(xml);
    String namesString = xml.get(XML_ITEM_NAME);
    
    if(namesString.contains(SEPARATOR)) {
      String[] names = namesString.split(SEPARATOR);
      for (String name : names) {
        name = refineName(name, index);
        if (name == null) {
          continue;
        }
        if (!caseSensitive) {
          name = name.toLowerCase();
        }
        items.put(name, Item.getItem(name, item));
      }
    }
    else {
      item.name = refineName(item.name, index);
      if (item.name != null) {
        if (!caseSensitive) {
          item.name = item.name.toLowerCase();
        }
        items.put(item.name, item);
      }
    }
  }

  static Item parseItem(Xml xml) {
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
    if(min < -1) {
      min = 0;
    }
    return Item.getItem(name, type, min, max, msg, uri);
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
      List<String> chars = map.computeIfAbsent(firstCharOfKey, k -> new ArrayList<>());
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

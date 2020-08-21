package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Metadata {
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

  Metadata(Shield shield, Xml xml, String type) {
    load(shield, xml, type);
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
    String k2 = Util.stripEosNumbers(key);
    if (list.contains(Util.INDEX_PARM_MARKER + k2)) {
      return k2;
    }
    return null;
  }

  void load(Shield shield, Xml xml, String type) {
    Util.initA2Zindex(index);

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
      processItem(shield, item);
    }
  }

  private void processItem(Shield shield, String item) {
    List<String> parms = Util.split(item);
    for (String parm : parms) {
      String k = null;
      String v = null;

      int pos = parm.indexOf('=');
      if (pos <= 0) {
        k = parm;
        v = Datatype.STRING.name();
      } else {
        k = parm.substring(0, pos);
        v = parm.substring(pos + 1, parm.length());
      }
      k = Util.refineName(k, index);
      if (k == null) {
        continue;
      }

      String maxMin = null;
      int endPos = v.lastIndexOf(Datatype.SEP_END_CHAR);
      if (endPos > 0) {
        maxMin = v.substring(endPos + Datatype.SEP_END_CHAR.length(), v.length());
        v = v.substring(0, endPos + Datatype.SEP_END_CHAR.length());
      } else {
        maxMin = v.substring(1, v.length());
        v = v.substring(0, 1);
      }

      processType(shield, k, v, parseMin(maxMin), parseMax(maxMin));
    }
  }

  private void processType(Shield shield, String key, String value, int min, int max) {
    if (!caseSensitive) {
      key = key.toLowerCase();
    }
    Datatype t = Datatype.get(value.toLowerCase());
    if (t != null) {
      if (t.name().equals(Datatype.NUMERIC_DELIMITED.name())) {
        addNumericDelimitersToType(shield, key, value.toLowerCase());
      } else if (t.name().equals(Datatype.ALPHANUMERIC_AND_MORE.name())) {
        addAlphaNumericAndMoreParmToType(shield, key, value.toLowerCase());
      } else if (t.name().equals(Datatype.REGEX.name())) {
        addRegexParmToType(shield, key, value.toLowerCase());
      } else if (t.name().equals(Datatype.JAVA.name())) {
        String clazz = value.substring(value.indexOf(Datatype.TYPE_JAVA) + Datatype.TYPE_JAVA.length(), value.length() - 1);
        shield.parmsUsingJava.put(key, Util.getJavaClassMethod(clazz));
      } else if (t.name().equals(Datatype.CONSTANT.name())) {
        addConstantToType(shield, key, value);
      }
      map.put(key, new Parameter(t, max, min));
    }
  }

  private int parseMax(String s) {
    int start = s.indexOf(Datatype.LENGTH_START_CHAR);
    if (start == 0) {
      int coma = s.indexOf(Datatype.LENGTH_MID_CHAR, start + Datatype.LENGTH_START_CHAR.length());
      if (coma > 0) {
        int end = s.indexOf(Datatype.LENGTH_END_CHAR, coma);
        if (end > 0) {
          int i = Integer.parseInt(s.substring(coma + Datatype.LENGTH_MID_CHAR.length(), s.length() - 1));
          if(i == -1) { i = Integer.MAX_VALUE; }
          return i;
        }
      }
    }
    return Integer.MAX_VALUE;
  }

  private int parseMin(String s) {
    int start = s.indexOf(Datatype.LENGTH_START_CHAR);
    if (start == 0) {
      int coma = s.indexOf(Datatype.LENGTH_MID_CHAR, start + Datatype.LENGTH_START_CHAR.length());
      if (coma > 0) {
        int i = Integer.parseInt(s.substring(start + Datatype.LENGTH_START_CHAR.length(), coma));
        if(i == -1) { 
          i = Integer.MAX_VALUE; 
        }
        return i;
      }
    }
    return 0;
  }

  private void addNumericDelimitersToType(Shield shield, String key, String value) {
    int start = value.indexOf(Datatype.SEP_START_CHAR);
    if (start > 0) {
      int end = value.indexOf(Datatype.SEP_END_CHAR);
      if (end > start) {
        String d = value.substring(start + Datatype.SEP_START_CHAR.length(), end);
        if (d != null && d.length() > 0) {
          Map<String, String> m = Datatype.numericDelimiters.get(shield.name);
          if (m == null) {
            m = new HashMap<>();
            Datatype.numericDelimiters.put(shield.name, m);
          }
          m.put(key, d);
        }
      }
    }
  }

  private void addAlphaNumericAndMoreParmToType(Shield shield, String key, String value) {
    int start = value.indexOf(Datatype.SEP_START_CHAR);
    if (start > 0) {
      int end = value.lastIndexOf(Datatype.SEP_END_CHAR);
      if (end > start) {
        Map<String, char[]> m = Datatype.alphanumericAndMoreChars.get(shield.name);
        if (m == null) {
          m = new HashMap<>();
          Datatype.alphanumericAndMoreChars.put(shield.name, m);
        }
        //handle special chars
        char[] array = Datatype.getAlphaNumericAndMoreCharArray(value.substring(start + Datatype.SEP_START_CHAR.length(), end));
        m.put(key, array);
      }
    }
  }
  
  private void addRegexParmToType(Shield shield, String key, String value) {
    int start = value.indexOf(Datatype.TYPE_REGEX);
    if (start >= 0) {
      shield.parmsUsingRegex.put(key, value.substring(start + Datatype.TYPE_REGEX.length(), value.length() - 1).toLowerCase());
    }
  }

  private void addConstantToType(Shield shield, String key, String value) {
    int start = value.indexOf(Datatype.TYPE_CONSTANT);
    if (start >= 0) {
      String s = value.substring(start + Datatype.TYPE_CONSTANT.length(), value.length() - 1);
      List<String> list = new ArrayList<>(Arrays.asList(s.split(",")));
      shield.parmsUsingConstants.put(key, list);
    }
  }

}

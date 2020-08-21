package com.sanwaf.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;

enum Datatype {
  NUMERIC {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value == null) {
        return points;
      }
      final int len = value.length();
      int errStart = -1;
      boolean foundDot = false;
      boolean foundNeg = false;
      int i = 0;
      for (i = 0; i < len; i++) {
        char c = value.charAt(i);
        int d = c - '0';
        if (d < 0 || d > 9) {
          if (!foundDot && c == '.') {
            foundDot = true;
          } else {
            if (!foundNeg && i == 0 && c == '-') {
              foundNeg = true;
            }
            if (errStart < 0) {
              errStart = i;
            }
          }
        } else {
          if (errStart >= 0) {
            points.add(new Point(errStart, i));
            errStart = -1;
          }
        }
      }
      if (errStart >= 0) {
        points.add(new Point(errStart, i));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      boolean foundDot = false;
      for (int i = 0; i < length; i++) {
        char c = value.charAt(i);
        int d = c - '0';
        if (d < 0 || d > 9) {
          if (i == 0 && c == '-') {
            continue;
          } else if (c == '.' && !foundDot) {
            foundDot = true;
          } else {
            return true;
          }
        }
      }
      return false;
    }
  },

  NUMERIC_DELIMITED {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      String d = numericDelimiters.get(shield.name).get(key);
      if (d == null) {
        return NUMERIC.getErrorHighlightPoints(shield, key, value);
      }
      if (value != null) {
        String[] ns = value.split(d, -1);
        for (String n : ns) {
          if (n.length() > 0) {
            points.addAll(NUMERIC.getErrorHighlightPoints(shield, key, n));
          }
        }
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      String[] ns = splitNumericDelimited(shield, key, value);
      for (String n : ns) {
        if (NUMERIC.inError(req, shield, key, n, n.length())) {
          return true;
        }
      }
      return false;
    }
  },

  ALPHANUMERIC {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value == null || value.length() == 0) {
        return points;
      }
      int start = -1;
      int len = value.length();
      int i = 0;
      for (i = 0; i < len; i++) {
        char c = value.charAt(i);
        if (Util.isCharNotAlphanumeric(c)) {
          if (start < 0) {
            start = i;
          }
        } else {
          if (start >= 0) {
            points.add(new Point(start, i));
            start = -1;
          }
        }
      }
      if (start >= 0) {
        points.add(new Point(start, i));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      int i = 0;
      for (i = 0; i < length; i++) {
        char c = value.charAt(i);
        if (Util.isCharNotAlphanumeric(c)) {
          return true;
        }
      }
      return false;
    }
  },

  ALPHANUMERIC_AND_MORE {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value == null || value.length() == 0) {
        return points;
      }
      Map<String, char[]> map = alphanumericAndMoreChars.get(shield.name);
      char[] chars = map.get(key);
      if (chars == null) {
        chars = new char[0];
      }
      int start = -1;
      int len = value.length();
      int i = 0;
      for (i = 0; i < len; i++) {
        char c = value.charAt(i);
        if (Util.isCharNotAlphanumeric(c)) {
          boolean pass = false;
          for (char cs : chars) {
            if (c == cs) {
              pass = true;
              break;
            }
          }
          if (!pass) {
            if (start < 0) {
              start = i;
            }
          } else {
            if (start >= 0) {
              points.add(new Point(start, i));
              start = -1;
            }
          }
        } else {
          if (start >= 0) {
            points.add(new Point(start, i));
            start = -1;
          }
        }
      }
      if (start >= 0) {
        points.add(new Point(start, i));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      Map<String, char[]> map = alphanumericAndMoreChars.get(shield.name);
      char[] chars = map.get(key);
      if (chars == null) {
        chars = new char[0];
      }
      for (int i = 0; i < length; i++) {
        char c = value.charAt(i);
        if (Util.isCharNotAlphanumeric(c)) {
          boolean pass = false;
          for (char cs : chars) {
            if (c == cs) {
              pass = true;
              break;
            }
          }
          if (!pass) {
            return true;
          }
        }
      }
      return false;
    }
  },

  CHAR {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value != null && value.length() > 0) {
        points.add(new Point(0, value.length()));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      return (value.length() > 1);
    }
  },

  STRING {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value == null || value.length() == 0) {
        return points;
      }

      for (Pattern p : shield.patterns) {
        Matcher m = p.matcher(value);
        while (m.find()) {
          int start = m.start();
          int end = m.end();
          points.add(new Point(start, end));
        }
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      for (Pattern p : shield.patterns) {
        if (p.matcher(value).find()) {
          return true;
        }
      }
      return false;
    }
  },

  REGEX {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value == null || value.length() == 0) {
        return points;
      }
      Pattern p = shield.customPatterns.get(shield.parmsUsingRegex.get(key));
      Matcher m = p.matcher(value);
      if (!m.find()) {
        points.add(new Point(0, value.length()));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      Pattern p = shield.customPatterns.get(shield.parmsUsingRegex.get(key));
      return !p.matcher(value).find();
    }
  },

  JAVA {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value != null) {
        points.add(new Point(0, value.length()));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      Method method = shield.parmsUsingJava.get(key);
      return Util.runJavaMethod(method, value, req);
    }
  },

  CONSTANT {
    @Override
    public List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value) {
      List<Point> points = new ArrayList<>();
      if (value != null) {
        points.add(new Point(0, value.length()));
      }
      return points;
    }

    @Override
    public boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length) {
      List<String> list = shield.parmsUsingConstants.get(key);
      return !list.contains(value);
    }
  };

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

  static final String HIGHLIGHT_START = "<mark>";
  static final String HIGHLIGHT_END = "</mark>";
  static final String HIGHLIGHT_START_MARKER = "~~#!~~start-mark-start~~#!~~";
  static final String HIGHLIGHT_END_MARKER = "~~#!~~end-mark-end~~#!~~";
  static final String SEP_START_CHAR = "{";
  static final String SEP_END_CHAR = "}";
  static final String LENGTH_START_CHAR = "(";
  static final String LENGTH_MID_CHAR = ",";
  static final String LENGTH_END_CHAR = ")";

  static final String TYPE_NUMERIC = "n";
  static final String TYPE_NUMERIC_DELIMITED = "n{";
  static final String TYPE_ALPHANUMERIC = "a";
  static final String TYPE_ALPHANUMERIC_AND_MORE = "a{";
  static final String TYPE_STRING = "s";
  static final String TYPE_CHAR = "c";
  static final String TYPE_REGEX = "r{";
  static final String TYPE_JAVA = "j{";
  static final String TYPE_CONSTANT = "k{";
  
  static final String TYPE_ALPHANUMERIC_AND_MORE_SPACE = "\\s";
  static final String TYPE_ALPHANUMERIC_AND_MORE_TAB = "\\t";
  static final String TYPE_ALPHANUMERIC_AND_MORE_NEWLINE = "\\n";
  static final String TYPE_ALPHANUMERIC_AND_MORE_CR = "\\r";
  static final String TYPE_ALPHANUMERIC_AND_MORE_SPACE_LONG = "<space>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_TAB_LONG = "<tab>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_NEWLINE_LONG = "<newline>";
  static final String TYPE_ALPHANUMERIC_AND_MORE_CR_LONG = "<carriage return>";

  static Map<String, Map<Datatype, String>> errorMessages = new HashMap<>();
  static Map<Datatype, String> defaultErrorMessages = new EnumMap<>(Datatype.class);
  static Map<String, Map<String, char[]>> alphanumericAndMoreChars = new HashMap<>();
  static Map<String, Map<String, String>> numericDelimiters = new HashMap<>();

  abstract List<Point> getErrorHighlightPoints(final Shield shield, final String key, final String value);

  abstract boolean inError(final ServletRequest req, final Shield shield, final String key, final String value, int length);

  static Datatype get(String s) {
    int start = s.indexOf(SEP_START_CHAR);
    if (start > 0) {
      s = s.substring(0, start + SEP_START_CHAR.length()).toLowerCase(Locale.ENGLISH);
      if (TYPE_NUMERIC_DELIMITED.equals(s)) {
        return NUMERIC_DELIMITED;
      } else if (TYPE_ALPHANUMERIC_AND_MORE.equals(s)) {
        return ALPHANUMERIC_AND_MORE;
      } else if (TYPE_REGEX.equals(s)) {
        return REGEX;
      } else if (TYPE_JAVA.equals(s)) {
        return JAVA;
      } else if (TYPE_CONSTANT.equals(s)) {
        return CONSTANT;
      }
    } else {
      if (TYPE_NUMERIC.equals(s)) {
        return NUMERIC;
      } else if (TYPE_ALPHANUMERIC.equals(s)) {
        return ALPHANUMERIC;
      } else if (TYPE_CHAR.equals(s)) {
        return CHAR;
      } else if (TYPE_STRING.equals(s)) {
        return STRING;
      }
    }
    return null;
  }

  static void setDefaultErrorMessages(Xml xml) {
    Xml msgBlockXml = new Xml(xml.get(XML_ERROR_MSG));
    defaultErrorMessages.put(ALPHANUMERIC, msgBlockXml.get(XML_ERROR_MSG_ALHPANUMERIC));
    defaultErrorMessages.put(ALPHANUMERIC_AND_MORE, msgBlockXml.get(XML_ERROR_MSG_ALPHANUMERIC_AND_MORE));
    defaultErrorMessages.put(CHAR, msgBlockXml.get(XML_ERROR_MSG_CHAR));
    defaultErrorMessages.put(NUMERIC, msgBlockXml.get(XML_ERROR_MSG_NUMERIC));
    defaultErrorMessages.put(NUMERIC_DELIMITED, msgBlockXml.get(XML_ERROR_MSG_NUMERIC_DELIMITED));
    defaultErrorMessages.put(STRING, msgBlockXml.get(XML_ERROR_MSG_STRING));
    defaultErrorMessages.put(REGEX, msgBlockXml.get(XML_ERROR_MSG_REGEX));
    defaultErrorMessages.put(JAVA, msgBlockXml.get(XML_ERROR_MSG_JAVA));
    defaultErrorMessages.put(CONSTANT, msgBlockXml.get(XML_ERROR_MSG_CONSTANT));
  }

  static void setShieldErrorMessages(Xml xml, String shieldName) {
    String msgBlock = xml.get(XML_ERROR_MSG);
    Xml msgBlockXml = new Xml(msgBlock);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_ALHPANUMERIC, ALPHANUMERIC, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_ALPHANUMERIC_AND_MORE, ALPHANUMERIC_AND_MORE, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_CHAR, CHAR, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_NUMERIC, NUMERIC, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_NUMERIC_DELIMITED, NUMERIC_DELIMITED, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_STRING, STRING, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_REGEX, REGEX, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_JAVA, JAVA, shieldName);
    putShieldErrorMessage(msgBlockXml, XML_ERROR_MSG_CONSTANT, CONSTANT, shieldName);
  }

  private static void putShieldErrorMessage(Xml xml, String key, Datatype type, String shieldName) {
    String s = xml.get(key);
    if (s.length() > 0) {
      Map<Datatype, String> map = errorMessages.get(shieldName);
      if (map == null) {
        map = new EnumMap<>(Datatype.class);
      }
      map.put(type, s);
      errorMessages.put(shieldName, map);
    }
  }

  static String getErrorMessage(final Shield shield, final String key, final Datatype type) {
    Map<Datatype, String> m = errorMessages.get(shield.name);
    String err;
    if (m != null) {
      err = m.get(type);
    } else {
      err = defaultErrorMessages.get(type);
    }
    if (type.equals(ALPHANUMERIC_AND_MORE)) {
      return substituteAlphaNumericAndMoreMessageData(shield, key, err);
    } else if (type.equals(NUMERIC_DELIMITED)) {
      return substituteAlphanumericAndMoreErrorMessage(shield, key, err);
    } else if (type.equals(CONSTANT)) {
      return substituteConstantErrorMessage(shield, key, err);
    }
    return err;
  }

  static String substituteAlphaNumericAndMoreMessageData(final Shield shield, final String key, final String errorString) {
    int i = errorString.indexOf(XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      Map<String, char[]> map = alphanumericAndMoreChars.get(shield.name);
      char[] chars = map.get(key);
      if (chars == null) {
        chars = new char[0];
      }
      return errorString.substring(0, i) + Util.jsonEncode(handleSpecialChars(chars)) + errorString.substring(i + XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }
  
  private static String handleSpecialChars(char[] chars) {
    String s = String.valueOf(chars);
    s = replaceString(s, " ", TYPE_ALPHANUMERIC_AND_MORE_SPACE_LONG);
    s = replaceString(s, "\t", TYPE_ALPHANUMERIC_AND_MORE_TAB_LONG);
    s = replaceString(s, "\n", TYPE_ALPHANUMERIC_AND_MORE_NEWLINE_LONG);
    s = replaceString(s, "\r", TYPE_ALPHANUMERIC_AND_MORE_CR_LONG);
    return s;
  }
  
  static char[] getAlphaNumericAndMoreCharArray(String s) {
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_SPACE, " ");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_TAB, "\t");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_NEWLINE, "\n");
    s = replaceString(s, TYPE_ALPHANUMERIC_AND_MORE_CR, "\r");
    return s.toCharArray();
  }

  static String replaceString(String s, String from, String to) {
    int i = s.indexOf(from);
    if( i >= 0 ) {
      s = s.substring(0, i) + to + s.substring(i + from.length(), s.length());
    }
    return s;
  }
    
  static String substituteAlphanumericAndMoreErrorMessage(final Shield shield, final String key, final String errorString) {
    int i = errorString.indexOf(XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      Map<String, String> m = numericDelimiters.get(shield.name);
      String s = m.get(key);
      if (s == null) {
        s = "";
      }
      return errorString.substring(0, i) + Util.jsonEncode(s) + errorString.substring(i + XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }

  static String substituteConstantErrorMessage(final Shield shield, final String key, final String errorString) {
    int i = errorString.indexOf(XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER);
    if (i >= 0) {
      List<String> list = shield.parmsUsingConstants.get(key);
      String s = list.toString();
      if (s == null) {
        s = "";
      }
      return errorString.substring(0, i) + Util.jsonEncode(s) + errorString.substring(i + XML_ERROR_MSG_DELIMITAED_PROPS_PLACEHOLDER.length(), errorString.length());
    }
    return errorString;
  }

  static boolean isSizeError(Shield shield, Datatype type, String key, String value, int length, int max, int min) {
    if (type == Datatype.NUMERIC_DELIMITED) {
      String[] array = splitNumericDelimited(shield, key, value);
      for (String n : array) {
        if (n.length() < min || n.length() > max) {
          return true;
        }
      }
    } else if (length < min || length > max) {
      return true;
    }
    return false;
  }

  private static String[] splitNumericDelimited(Shield shield, String key, String value) {
    String[] array;
    String s = numericDelimiters.get(shield.name).get(key);
    if (s == null) {
      array = new String[] { value };
    } else {
      array = value.split(s);
    }
    return array;
  }
}

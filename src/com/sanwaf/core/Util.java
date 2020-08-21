package com.sanwaf.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletRequest;

final class Util {
  static final String SPLIT_LINE_CHARS = ":::";
  static final String INDEX_PARM_MARKER = "  ";
  static final String STAR = "*";

  private Util() {
    /* do not instantiate this class */
  }

  static void appendPItemMapToSB(Map<String, Parameter> map, StringBuilder sb, String label) {
    sb.append(label);
    if (map == null || map.size() == 0) {
      sb.append("\n\t\t(none found)");
    } else {
      Iterator<?> it = map.entrySet().iterator();
      while (it.hasNext()) {
        @SuppressWarnings("unchecked")
        Map.Entry<String, Parameter> e = (Map.Entry<String, Parameter>) it.next();
        sb.append("\n\t\t" + e.getKey() + "=" + e.getValue());
      }
    }
    sb.append("\n");
  }

  static List<String> split(String s) {
    List<String> out = new ArrayList<>();
    if (s != null && s.length() > 0) {
      String[] vs = s.split(SPLIT_LINE_CHARS);
      for (String v : vs) {
        if (v.length() > 0) {
          out.add(v);
        }
      }
    }
    return out;
  }

  static void initA2Zindex(Map<String, List<String>> map) {
    for (char ch = 'a'; ch <= 'z'; ++ch) {
      map.put(String.valueOf(ch), null);
    }
  }

  static int parseInt(String s, int d) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      return d;
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
        if (!Util.isNotAlphanumeric(markerChars)) {
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

  static String jsonEncode(String s) {
    if (s == null) {
      return "";
    } else {
      s = s.replace("\\", "\\\\");
      s = s.replace("\"", "\\\"");
      return s.replace("/", "\\/");
    }
  }

  static String getSortOfRandomNumber() {
    try {
      java.security.SecureRandom srandom = new java.security.SecureRandom();
      return String.format("%03d", srandom.nextInt(99999)) + "-" + String.format("%03d", srandom.nextInt(9999));
    } catch (IllegalFormatException e) {
      return String.valueOf(UUID.randomUUID());
    }
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

  static boolean isCharNotAlphanumeric(char c) {
    return (c < 0x30 || (c >= 0x3a && c <= 0x40) || (c > 0x5a && c <= 0x60) || c > 0x7a);
  }

  static boolean runJavaMethod(Method method, String v, ServletRequest req) {
    try {
      Object o = method.invoke(null, v, req);
      return Boolean.valueOf(String.valueOf(o));
    } catch (NullPointerException | IllegalAccessException | InvocationTargetException e) {
      return true;
    }
  }

  static Method getJavaClassMethod(String sClazzAndMethod) {
    Class<?> clazz;
    try {
      clazz = Class.forName(parseClazz(sClazzAndMethod));
    } catch (ClassNotFoundException e) {
      return null;
    }

    try {
      return clazz.getMethod(parseMethod(sClazzAndMethod), String.class, ServletRequest.class);
    } catch (NullPointerException | NoSuchMethodException e) {
      return null;
    }
  }

  static String parseClazz(String s) {
    if (s == null) {
      return "";
    }
    int last = s.lastIndexOf('.');
    if (last > 0) {
      return s.substring(0, last);
    }
    return s;
  }

  static String parseMethod(String s) {
    int start = s.lastIndexOf('.');
    if (start > 0) {
      int end = s.lastIndexOf('(');
      if (end > 0) {
        return s.substring(start + 1, end);
      }
    }
    return s;
  }

  static String stripXmlComments(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    return s.replaceAll("<!--.*-->", "").replaceAll("<!--((?!<!--)[\\s\\S])*-->", "");
  }

  static String readFile(InputStream is) throws IOException {
    StringBuilder sb = new StringBuilder();
    int read = 0;
    byte[] data = new byte[1024];
    while (true) {
      read = is.read(data);
      if (read < 0) {
        break;
      }
      sb.append(new String(data));
      data = new byte[1024];
    }
    is.close();
    return sb.toString();
  }

}

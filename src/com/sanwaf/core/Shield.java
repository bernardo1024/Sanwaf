package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.sanwaf.log.Logger;

final class Shield {
  static final String SPLIT_LINE_CHARS = ":::";
  Sanwaf sanwaf = null;
  Logger logger = null;
  String name = null;
  int minLen = 0;
  int maxLen = 0;
  int regexMinLen = 0;
  boolean regexAlways = false;
  List<String> regexAlwaysExclusions = new ArrayList<>();
  List<Pattern> patterns = new ArrayList<>();
  Map<String, Pattern> customPatterns = new HashMap<>();
  Metadata parameters = null;
  Metadata cookies = null;
  Metadata headers = null;

  Shield(Sanwaf sanwaf, Xml xml, Logger logger) {
    this.sanwaf = sanwaf;
    this.logger = logger;
    load(xml);
    logStartup();
  }

  boolean threatDetected(ServletRequest req) {
    return ((parameters.enabled && parameterThreatDetected(req)) || (headers.enabled && headerThreatDetected(req)) || (cookies.enabled && cookieThreatDetected(req)));
  }

  private boolean parameterThreatDetected(ServletRequest req) {
    String k = null;
    String[] values = null;
    Enumeration<?> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      k = (String) names.nextElement();
      values = req.getParameterValues(k);
      for (String v : values) {
        if (threat(req, parameters, k, v)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean headerThreatDetected(ServletRequest req) {
    Enumeration<?> names = ((HttpServletRequest) req).getHeaderNames();
    while (names.hasMoreElements()) {
      String s = String.valueOf(names.nextElement());
      Enumeration<?> items = ((HttpServletRequest) req).getHeaders(s);
      while (items.hasMoreElements()) {
        if (threat(req, headers, s, (String) items.nextElement())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean cookieThreatDetected(ServletRequest req) {
    Cookie[] items = ((HttpServletRequest) req).getCookies();
    if (items != null) {
      for (Cookie c : items) {
        if (threat(req, cookies, c.getName(), c.getValue())) {
          return true;
        }
      }
    }
    return false;
  }

  boolean threat(String v) {
    return threat(null, null, "", v);
  }

  boolean threat(ServletRequest req, Metadata meta, String key, String value) {
    if (key == null || value == null) {
      return false;
    }
    int len = value.length();
    if (len < minLen || len > maxLen) {
      return false;
    }
    Parameter parm;
    if (meta != null) {
      parm = getParameterFromMetadata(meta, key);
      if (parm == null) {
        String a = meta.getFromIndex(key);
        if (a == null) {
          return false;
        }
        parm = getParameterFromMetadata(meta, a);
        if (parm == null) {
          return false;
        }
      }
    } else {
      parm = new ParameterString();
    }
    if (parm.isSizeError(value)) {
      return true;
    }
    return parm.inError(req, this, value);
  }

  private Parameter getParameterFromMetadata(Metadata meta, String key) {
    Parameter parm;
    parm = getParameter(meta, key);
    if (parm == null && regexAlways && !regexAlwaysExclusions.contains(key)) {
      parm = new ParameterString();
    }
    return parm;
  }

  Parameter getParameter(Metadata meta, String key) {
    Parameter parm;
    if (meta.caseSensitive) {
      parm = meta.map.get(key);
    } else {
      parm = meta.map.get(key.toLowerCase());
    }
    return parm;
  }

  List<Error> getErrors(ServletRequest req, String key, String value) {
    List<Error> errors = new ArrayList<>();
    Error err = getErrorForMetadata(req, parameters, key, value);
    if (err != null) {
      errors.add(err);
    }
    err = getErrorForMetadata(req, headers, key, value);
    if (err != null) {
      errors.add(err);
    }
    err = getErrorForMetadata(req, cookies, key, value);
    if (err != null) {
      errors.add(err);
    }
    return errors;
  }

  private Error getErrorForMetadata(ServletRequest req, Metadata meta, String key, String value) {
    Parameter p = getParameter(meta, key);
    if (p == null) {
      if (regexAlways) {
        p = new ParameterString();
      } else {
        return null;
      }
    }
    if (p.inError(req, this, value)) {
      return new Error(this, p, key, value);
    }
    return null;
  }

  // XML LOAD CODE
  static final String XML_ITEM = "item";
  static final String XML_ITEM_NAME = "name";
  static final String XML_ITEM_TYPE = "type";
  static final String XML_ITEM_MAX = "max";
  static final String XML_ITEM_MIN = "min";
  static final String XML_ITEM_MSG = "msg";
  static final String XML_ITEM_PATH = "path";
  static final String XML_KEY = "key";
  static final String XML_VALUE = "value";
  static final String XML_NAME = "name";
  static final String XML_MIN_LEN = "minLen";
  static final String XML_MAX_LEN = "maxLen";
  static final String XML_MAX_LEN_LOG = "maxLenLogViolation";
  static final String XML_MAX_LEN_FAIL = "maxLenFailOnViolation";
  static final String XML_REGEX = "regex";
  static final String XML_REGEX_ALWAYS_REGEX = "alwaysPerformRegex";
  static final String XML_REGEX_ALWAYS_REGEX_EXCLUSIONS = "exclusions";
  static final String XML_REGEX_PATTERNS_AUTO = "autoRunPatterns";
  static final String XML_REGEX_PATTERNS_CUSTOM = "customPatterns";

  private void load(Xml xml) {
    name = String.valueOf(xml.get(XML_NAME));
    maxLen = parseInt(xml.get(XML_MAX_LEN), maxLen);
    if (maxLen == -1) {
      maxLen = Integer.MAX_VALUE;
    }
    minLen = parseInt(xml.get(XML_MIN_LEN), minLen);
    if (minLen == -1) {
      minLen = Integer.MAX_VALUE;
    }

    String regexBlock = xml.get(XML_REGEX);
    Xml regexBlockXml = new Xml(regexBlock);
    loadPatterns(regexBlockXml);
    regexMinLen = parseInt(regexBlockXml.get(XML_MIN_LEN), regexMinLen);
    if (regexMinLen == -1) {
      regexMinLen = Integer.MAX_VALUE;
    }

    String alwaysBlock = xml.get(XML_REGEX_ALWAYS_REGEX);
    Xml alwaysBlockXml = new Xml(alwaysBlock);
    regexAlways = Boolean.parseBoolean(alwaysBlockXml.get(Metadata.XML_ENABLED));
    regexAlwaysExclusions = new ArrayList<>();
    if (regexAlways) {
      String exclusionsBlock = alwaysBlockXml.get(XML_REGEX_ALWAYS_REGEX_EXCLUSIONS);
      Xml exclusionsBlockXml = new Xml(exclusionsBlock);
      String[] items = exclusionsBlockXml.getAll(XML_ITEM);
      for (String item : items) {
        List<String> list = split(item);
        for (String l : list) {
          regexAlwaysExclusions.add(l);
        }
      }
    }
    parameters = new Metadata(xml, Metadata.XML_PARAMETERS);
    cookies = new Metadata(xml, Metadata.XML_COOKIES);
    headers = new Metadata(xml, Metadata.XML_HEADERS);
    Error.setShieldErrorMessages(xml, name);
  }

  private void loadPatterns(Xml xml) {
    String autoBlock = xml.get(XML_REGEX_PATTERNS_AUTO);
    Xml autoBlockXml = new Xml(autoBlock);
    String[] items = autoBlockXml.getAll(XML_ITEM);
    for (String item : items) {
      Xml itemBlockXml = new Xml(item);
      String value = itemBlockXml.get(XML_VALUE);
      List<String> list = split(value);
      for (String l : list) {
        patterns.add(Pattern.compile(l, Pattern.CASE_INSENSITIVE));
      }
    }
    String customBlock = xml.get(XML_REGEX_PATTERNS_CUSTOM);
    Xml customBlockXml = new Xml(customBlock);
    items = customBlockXml.getAll(XML_ITEM);
    for (String item : items) {
      Xml itemBlockXml = new Xml(item);
      String key = itemBlockXml.get(XML_KEY);
      String value = itemBlockXml.get(XML_VALUE);
      List<String> list = split(value);
      for (String l : list) {
        customPatterns.put(key.toLowerCase(), Pattern.compile(l, Pattern.CASE_INSENSITIVE));
      }
    }
  }

  private void logStartup() {
    StringBuilder sb = new StringBuilder();
    sb.append("Loading Shield: ").append(name);
    if (sanwaf.verbose) {
      sb.append("\nSettings:\n");
      sb.append("\t").append(XML_MAX_LEN).append("=").append(maxLen).append("\n");
      sb.append("\t").append(XML_MIN_LEN).append("=").append(minLen).append("\n");
      sb.append("\t").append("regex ").append(XML_MIN_LEN).append("=").append(regexMinLen).append("\n");
      sb.append("\t").append(Metadata.XML_PARAMETERS).append(".").append(Metadata.XML_ENABLED).append("=").append(parameters.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_PARAMETERS).append(".").append(Metadata.XML_CASE_SENSITIVE).append("=").append(parameters.caseSensitive).append("\n");
      sb.append("\t").append(Metadata.XML_COOKIES).append(".").append(Metadata.XML_ENABLED).append("=").append(cookies.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_COOKIES).append(".").append(Metadata.XML_CASE_SENSITIVE).append("=").append(cookies.caseSensitive).append("\n");
      sb.append("\t").append(Metadata.XML_HEADERS).append(".").append(Metadata.XML_ENABLED).append("=").append(headers.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_HEADERS).append(".").append(Metadata.XML_CASE_SENSITIVE).append("=").append(headers.caseSensitive).append("\n");

      int dot = 0;
      String propsRegex = XML_REGEX_PATTERNS_AUTO + ".";
      sb.append("\nPatterns:\n");
      for (Pattern pattern : patterns) {
        sb.append("\t").append(propsRegex).append(dot++).append("=").append(pattern).append("\n");
      }

      sb.append("\n" + XML_REGEX_PATTERNS_CUSTOM + ":\n");
      Iterator<Map.Entry<String, Pattern>> it = customPatterns.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, Pattern> pair = it.next();
        sb.append("\t").append(pair.getKey()).append("=").append(pair.getValue()).append("\n");
      }

      if (regexAlways) {
        sb.append("\n\tShield Secured List: *Ignored*");
        sb.append("\n\tRegexAlways=true (process all parameters)");
        sb.append("\n\tExcept for (exclusion list):\n");
        for (String s : regexAlwaysExclusions) {
          sb.append("\t").append(s);
        }
      }
      sb.append("\n");
      if (!regexAlways) {
        sb.append("Configured/Secured Entries:\n");
        appendPItemMapToSB(headers.map, sb, "\tHeaders");
        appendPItemMapToSB(cookies.map, sb, "\tCookies");
        appendPItemMapToSB(parameters.map, sb, "\tParameters");
      }
    }
    logger.info(sb.toString());
  }

  static int parseInt(String s, int d) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException nfe) {
      return d;
    }
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

}


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

import com.sanwaf.core.Sanwaf.AllowListType;
import com.sanwaf.log.Logger;

final class Shield {
  static final String XML_NAME = "name";
  static final String XML_MODE = "mode";
  static final String XML_MIN_LEN = "minLen";
  static final String XML_MAX_LEN = "maxLen";
  static final String XML_CHILD = "child";
  static final String XML_CHILD_SHIELD = "child-shield";
  static final String XML_SHIELD_SETTINGS = "shield-settings";
  static final String XML_REGEX_CONFIG = "regex-config";
  static final String XML_REGEX_ALWAYS_REGEX = "forceStringPatterns";
  static final String XML_REGEX_ALWAYS_REGEX_EXCLUSIONS = "exclusions";
  static final String XML_REGEX_PATTERNS_AUTO = "stringPatterns";
  static final String XML_REGEX_PATTERNS_CUSTOM = "customPatterns";

  static final String XML_KEY = "key";
  static final String XML_VALUE = "value";
  static final String XML_CASE_SENSITIVE = "caseSensitive";
  static final String XML_ENABLED = "enabled";
  static final String SEPARATOR = ":::";

  Sanwaf sanwaf = null;
  Logger logger = null;
  String name = null;
  Modes mode = Modes.BLOCK;
  Shield childShield = null;
  int minLen = 0;
  int maxLen = Integer.MAX_VALUE;
  int regexMinLen = 0;
  boolean regexAlways = false;
  Map<String, String> errorMessages = new HashMap<>();
  List<String> regexAlwaysExclusions = new ArrayList<>();
  Map<String, Rule> rulePatterns = new HashMap<>();
  Map<String, Rule> customRulePatterns = new HashMap<>();
  Metadata parameters = null;
  Metadata cookies = null;
  Metadata headers = null;
  MetadataEndpoints endpoints = null;

  Shield(Sanwaf sanwaf, Xml xml, Xml shieldXml, Logger logger) {
    this.sanwaf = sanwaf;
    this.logger = logger;
    load(sanwaf, xml, shieldXml, logger);
    logStartup();
  }

  boolean threatDetected(ServletRequest req) {
    return ((endpoints.enabled && endpointsThreatDetected(req)) 
        || (parameters.enabled && parameterThreatDetected(req)) 
        || (headers.enabled && headerThreatDetected(req))
        || (cookies.enabled && cookieThreatDetected(req)));
  }

  private boolean endpointsThreatDetected(ServletRequest req) {
    HttpServletRequest hreq = (HttpServletRequest) req;
    String uri = hreq.getRequestURI();
    Metadata metadata = endpoints.endpointParameters.get(uri);
    if (metadata == null) {
      return false;
    }

    String k = null;
    String[] values = null;
    Enumeration<?> names = req.getParameterNames();
    while (names.hasMoreElements()) {
      k = (String) names.nextElement();
      values = req.getParameterValues(k);
      for (String v : values) {
        if (threat(req, metadata, k, v, true)) {
          return true;
        }
      }
    }
    return false;
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
      Enumeration<?> headerEnumeration = ((HttpServletRequest) req).getHeaders(s);
      while (headerEnumeration.hasMoreElements()) {
        if (threat(req, headers, s, (String) headerEnumeration.nextElement())) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean cookieThreatDetected(ServletRequest req) {
    Cookie[] cookieArray = ((HttpServletRequest) req).getCookies();
    if (cookieArray != null) {
      for (Cookie c : cookieArray) {
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
    return threat(req, meta, key, value, false);
  }

  boolean threat(ServletRequest req, Metadata meta, String key, String value, boolean isEndpoint) {
    return threat(req, meta, key, value, isEndpoint, false);
  }

  boolean threat(ServletRequest req, Metadata meta, String key, String value, boolean isEndpoint, boolean forceStringPatterns) {
    if (value == null) {
      return false;
    }
    int len = value.length();
    if (len < minLen || len > maxLen) {
      return handleChildShield(req, value);
    }
    Item item;
    if (meta != null) {
      item = getItemFromMetaOrIndex(meta, key);
      if (item == null) {
        if (forceStringPatterns) {
          item = new ItemString();
        } else {
          return meta.endpointIsStrict && MetadataEndpoints.isStrictError(req, meta); 
        }
      }
    } else {
      item = new ItemString();
    }

    return isInError(req, meta, value, isEndpoint, item);
  }

  private boolean isInError(ServletRequest req, Metadata meta, String value, boolean isEndpoint, Item item) {
    if (item.required && value.length() == 0) {
      return true;
    }
    return (isEndpoint && isEndpointThreat(item, value, req, meta)) || item.inError(req, this, value);
  }

  private Item getItemFromMetaOrIndex(Metadata meta, String key) {
    Item item = getItemFromMetadata(meta, key);
    if (item == null) {
      String a = meta.getFromIndex(key);
      if (a == null) {
        return null;
      }
      item = getItemFromMetadata(meta, a);
      if (item == null) {
        return null;
      }
    }
    return item;
  }

  private boolean isEndpointThreat(Item item, String value, ServletRequest req, Metadata meta) {
    if (MetadataEndpoints.isStrictError(req, meta)) {
      return true;
    }

    return item.related.length() > 0 && !endpoints.isRelateValid(item.related, value, req, meta);
  }

  private boolean handleChildShield(ServletRequest req, String value) {
    if (childShield != null) {
      if (req == null) {
        return childShield.threat(value);
      } else {
        return childShield.threatDetected(req);
      }
    }
    return false;
  }

  private Item getItemFromMetadata(Metadata meta, String key) {
    Item item;
    item = getItem(meta, key);
    if (item == null && regexAlways && !regexAlwaysExclusions.contains(key)) {
      item = new ItemString();
    }
    return item;
  }

  static Modes getMode(String sMode, Modes def) {
    switch(sMode.toLowerCase()) {
    case "disabled":
      return Modes.DISABLED;
    case "block":
      return Modes.BLOCK;
    case "detect":
      return Modes.DETECT;
    case "detect-all":
      return Modes.DETECT_ALL;
    default:
      return def;
    }
  }
  
  String getAllowListedValue(String name, AllowListType type, HttpServletRequest req) {
    if (name == null || req == null) {
      return null;
    }

    if (type == AllowListType.HEADER) {
      return getAllowListedHeader(name, req);
    } else if (type == AllowListType.COOKIE) {
      return getAllowListedCookie(name, req);
    } else if (type == AllowListType.PARAMETER) {
      return getAllowListedParameter(name, req);
    }
    return null;
  }

  String getAllowListedHeader(String name, HttpServletRequest req) {
    Item item = getItemFromMetadata(headers, name);
    if (item != null) {
      return req.getHeader(name);
    }
    return null;
  }

  String getAllowListedCookie(String name, HttpServletRequest req) {
    Item item = getItemFromMetadata(cookies, name);
    if (item != null) {
      Cookie[] cookieValues = req.getCookies();
      if (cookieValues != null) {
        for (Cookie c : cookieValues) {
          if (c.getName().equals(name)) {
            return c.getValue();
          }
        }
      }
    }
    return null;
  }

  String getAllowListedParameter(String name, HttpServletRequest req) {
    Item item = getItemFromMetadata(parameters, name);
    if (item != null) {
      return req.getParameter(name);
    }
    return null;
  }

  Item getItem(Metadata meta, String key) {
    Item item;
    if (meta.caseSensitive) {
      item = meta.items.get(key);
    } else {
      item = meta.items.get(key.toLowerCase());
    }
    return item;
  }

  List<Error> getErrors(ServletRequest req, String key, String value) {
    return getErrors(req, key, value, false);
  }

  List<Error> getErrors(ServletRequest req, String key, String value, boolean forceRegexAlways) {
    List<Error> errors = new ArrayList<>();

    if (req != null) {
      HttpServletRequest hreq = (HttpServletRequest) req;
      String uri = hreq.getRequestURI();
      Metadata endpointsMeta = endpoints.endpointParameters.get(uri);
      if (endpointsMeta != null) {
        Error err = getErrorForMetadata(req, endpointsMeta, key, value, forceRegexAlways, true);
        if (err != null) {
          errors.add(err);
        }
      }
    }

    Error err = getErrorForMetadata(req, parameters, key, value, forceRegexAlways, false);
    if (err != null) {
      errors.add(err);
      if (forceRegexAlways) {
        return errors;
      }
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
    return getErrorForMetadata(req, meta, key, value, false, false);
  }

  private Error getErrorForMetadata(ServletRequest req, Metadata meta, String key, String value, boolean forceRegexAlways, boolean isEndpoint) {
    if (!threat(req, meta, key, value, isEndpoint, forceRegexAlways)) {
      return null;
    }

    Item item = getItem(meta, key);
    if (item == null) {
      if (regexAlways || forceRegexAlways) {
        item = new ItemString();
      } else {
        return null;
      }
    }
    return new Error(req, this, item, key, value);
  }

  // XML LOAD CODE
  private void load(Sanwaf sanwaf, Xml xml, Xml shieldXml, Logger logger) {
    Xml settingsBlockXml = new Xml(shieldXml.get(XML_SHIELD_SETTINGS));
    name = String.valueOf(settingsBlockXml.get(XML_NAME));
    mode = getMode(settingsBlockXml.get(XML_MODE), Modes.BLOCK);
    maxLen = parseInt(settingsBlockXml.get(XML_MAX_LEN), maxLen);
    if (maxLen == -1) {
      maxLen = Integer.MAX_VALUE;
    }
    minLen = parseInt(settingsBlockXml.get(XML_MIN_LEN), minLen);
    if (minLen == -1) {
      minLen = Integer.MAX_VALUE;
    }
    String childShieldName = settingsBlockXml.get(XML_CHILD);
    if (childShieldName.length() > 0) {
      loadChildShield(sanwaf, xml, childShieldName, logger);
    }

    Error.setErrorMessages(errorMessages, settingsBlockXml);

    Xml regexBlockXml = new Xml(shieldXml.get(XML_REGEX_CONFIG));
    loadPatterns(regexBlockXml);
    regexMinLen = parseInt(regexBlockXml.get(XML_MIN_LEN), regexMinLen);
    if (regexMinLen == -1) {
      regexMinLen = Integer.MAX_VALUE;
    }

    String alwaysBlock = shieldXml.get(XML_REGEX_ALWAYS_REGEX);
    Xml alwaysBlockXml = new Xml(alwaysBlock);
    regexAlways = Boolean.parseBoolean(alwaysBlockXml.get(XML_ENABLED));
    regexAlwaysExclusions = new ArrayList<>();
    if (regexAlways) {
      String exclusionsBlock = alwaysBlockXml.get(XML_REGEX_ALWAYS_REGEX_EXCLUSIONS);
      Xml exclusionsBlockXml = new Xml(exclusionsBlock);
      String[] items = exclusionsBlockXml.getAll(ItemFactory.XML_ITEM);
      for (String item : items) {
        List<String> list = split(item);
        for (String l : list) {
          regexAlwaysExclusions.add(l);
        }
      }
    }
    endpoints = new MetadataEndpoints(shieldXml, logger);
    parameters = new Metadata(shieldXml, Metadata.XML_PARAMETERS, logger);
    cookies = new Metadata(shieldXml, Metadata.XML_COOKIES, logger);
    headers = new Metadata(shieldXml, Metadata.XML_HEADERS, logger);
  }

  private void loadChildShield(Sanwaf sanwaf, Xml xml, String childShieldName, Logger logger) {
    String[] children = xml.getAll(XML_CHILD_SHIELD);
    for (String child : children) {
      Xml childXml = new Xml(child);
      Xml settings = new Xml(childXml.get(XML_SHIELD_SETTINGS));
      if (settings.get(XML_NAME).equals(childShieldName)) {
        childShield = new Shield(sanwaf, xml, new Xml(child), logger);
        break;
      }
    }
  }

  private void loadPatterns(Xml xml) {
    String autoBlock = xml.get(XML_REGEX_PATTERNS_AUTO);
    Xml autoBlockXml = new Xml(autoBlock);
    String[] items = autoBlockXml.getAll(ItemFactory.XML_ITEM);
    for (String item : items) {
      Xml itemBlockXml = new Xml(item);
      String value = itemBlockXml.get(XML_VALUE);
      String key = itemBlockXml.get(XML_KEY);
      Modes m = Shield.getMode(itemBlockXml.get(XML_MODE), Modes.BLOCK);
      List<String> list = split(value);
      for (String l : list) {
        rulePatterns.put(key, new Rule(m, Pattern.compile(l, Pattern.CASE_INSENSITIVE)));
      }
    }
    String customBlock = xml.get(XML_REGEX_PATTERNS_CUSTOM);
    Xml customBlockXml = new Xml(customBlock);
    items = customBlockXml.getAll(ItemFactory.XML_ITEM);
    for (String item : items) {
      Xml itemBlockXml = new Xml(item);
      String key = itemBlockXml.get(XML_KEY);
      String value = itemBlockXml.get(XML_VALUE);
      Modes m = Shield.getMode(itemBlockXml.get(XML_MODE), Modes.BLOCK);
      List<String> list = split(value);
      for (String l : list) {
        customRulePatterns.put(key.toLowerCase(), new Rule(m, Pattern.compile(l, Pattern.CASE_INSENSITIVE)));
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
      if (childShield != null) {
        sb.append("\t").append(XML_CHILD_SHIELD).append("=").append(childShield.name).append("\n");
      }
      sb.append("\t").append("regex ").append(XML_MIN_LEN).append("=").append(regexMinLen).append("\n");
      sb.append("\t").append(MetadataEndpoints.XML_ENDPOINTS).append(".").append(XML_ENABLED).append("=").append(endpoints.enabled).append("\n");
      sb.append("\t").append(MetadataEndpoints.XML_ENDPOINTS).append(".").append(XML_CASE_SENSITIVE).append("=").append(endpoints.caseSensitive).append("\n");
      sb.append("\t").append(Metadata.XML_PARAMETERS).append(".").append(XML_ENABLED).append("=").append(parameters.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_PARAMETERS).append(".").append(XML_CASE_SENSITIVE).append("=").append(parameters.caseSensitive).append("\n");
      sb.append("\t").append(Metadata.XML_COOKIES).append(".").append(XML_ENABLED).append("=").append(cookies.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_COOKIES).append(".").append(XML_CASE_SENSITIVE).append("=").append(cookies.caseSensitive).append("\n");
      sb.append("\t").append(Metadata.XML_HEADERS).append(".").append(XML_ENABLED).append("=").append(headers.enabled).append("\n");
      sb.append("\t").append(Metadata.XML_HEADERS).append(".").append(XML_CASE_SENSITIVE).append("=").append(headers.caseSensitive).append("\n");

      String propsRegex = XML_REGEX_PATTERNS_AUTO + ".";
      sb.append("\nPatterns:\n");
      for (Map.Entry<String, Rule> e : rulePatterns.entrySet()) {
        sb.append("\t").append(e.getValue().mode).append("\t").append(propsRegex).append(e.getKey()).append("=").append(e.getValue().pattern).append("\n");
      }

      sb.append("\n" + XML_REGEX_PATTERNS_CUSTOM + ":\n");
      for (Map.Entry<String, Rule> e : customRulePatterns.entrySet()) {
        sb.append("\t").append(e.getValue().mode).append("\t").append(propsRegex).append(e.getKey()).append("=").append(e.getValue().pattern).append("\n");
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
        appendPItemMapToSB(headers.items, sb, "\tHeaders");
        appendPItemMapToSB(cookies.items, sb, "\tCookies");
        appendPItemMapToSB(parameters.items, sb, "\tParameters");
        appendEndpoints(endpoints, sb, "\tEndpoints\t");
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

  static void appendEndpoints(MetadataEndpoints endpoints, StringBuilder sb, String label) {
    Iterator<Map.Entry<String, Metadata>> it = endpoints.endpointParameters.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Metadata> pair = it.next();
      appendPItemMapToSB(pair.getValue().items, sb, label + pair.getKey());
      // it.remove(); // avoids a ConcurrentModificationException
    }

  }

  static void appendPItemMapToSB(Map<String, Item> map, StringBuilder sb, String label) {
    sb.append(label);
    if (map == null || map.size() == 0) {
      sb.append("\n\t\t(none found)");
    } else {
      Iterator<?> it = map.entrySet().iterator();
      while (it.hasNext()) {
        @SuppressWarnings("unchecked")
        Map.Entry<String, Item> e = (Map.Entry<String, Item>) it.next();
        sb.append("\n\t\t" + e.getKey() + "=" + e.getValue());
      }
    }
    sb.append("\n");
  }

  static List<String> split(String s) {
    List<String> out = new ArrayList<>();
    if (s != null && s.length() > 0) {
      String[] vs = s.split(SEPARATOR);
      for (String v : vs) {
        if (v.length() > 0) {
          out.add(v);
        }
      }
    }
    return out;
  }
}

class Rule{
  Modes mode;
  Pattern pattern;
  Rule(Modes mode, Pattern patter){
    this.mode = mode;
    this.pattern = patter;
  }
}
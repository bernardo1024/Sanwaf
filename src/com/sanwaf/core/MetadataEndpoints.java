package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

public class MetadataEndpoints {
  static final String XML_ENDPOINTS = "endpoints";
  static final String XML_ENDPOINT = "endpoint";
  static final String XML_STRICT = "strict";

  boolean enabled = false;
  boolean caseSensitive = true;
  Map<String, Metadata> endpointParameters = new HashMap<>();

  MetadataEndpoints(Xml xml) {
    load(xml);
  }

  void load(Xml xml) {
    String metadataBlock = xml.get(Metadata.XML_METADATA);
    Xml metadataBlockXml = new Xml(metadataBlock);
    String securedBlock = metadataBlockXml.get(Metadata.XML_SECURED);
    Xml securedBlockXml = new Xml(securedBlock);

    String enabledViewBlock = metadataBlockXml.get(Shield.XML_ENABLED);
    Xml enabledViewdBlockXml = new Xml(enabledViewBlock);
    enabled = Boolean.parseBoolean(enabledViewdBlockXml.get(XML_ENDPOINTS));

    String caseBlock = metadataBlockXml.get(Shield.XML_CASE_SENSITIVE);
    Xml caseBlockXml = new Xml(caseBlock);
    caseSensitive = Boolean.parseBoolean(caseBlockXml.get(XML_ENDPOINTS));

    String endpointsBlock = securedBlockXml.get(XML_ENDPOINTS);
    Xml endpointsXml = new Xml(endpointsBlock);

    String[] xmlEndpoints = endpointsXml.getAll(XML_ENDPOINT);
    for (String endpointString : xmlEndpoints) {
      Xml endpointXml = new Xml(endpointString);
      String uri = endpointXml.get(Item.XML_ITEM_URI);
      String strict = endpointXml.get(XML_STRICT);
      String items = endpointXml.get(Item.XML_ITEMS);
      Metadata parameters = new Metadata(items, caseSensitive, true, strict);
      endpointParameters.put(uri, parameters);
    }
  }

  static boolean isStrictError(ServletRequest req, Metadata meta) {
    if (meta.endpointIsStrict) {
      if (!meta.endpointIsStrictAllowLess) {
        for (String name : meta.items.keySet()) {
          String s = req.getParameter(name);
          if (s == null) {
            return true;
          }
        }
      }

      Enumeration<?> names = req.getParameterNames();
      while (names.hasMoreElements()) {
        String k = (String) names.nextElement();
        if (meta.items.get(k) == null) {
          return true;
        }
      }
    }
    return false;
  }

  boolean isRelateValid(String related, String value, ServletRequest req, Metadata meta) {
    List<String> andBlocks = parseBlocks(related, 0, "AND", ")&&(", "(", ")");
    List<String> andOrBlocks = parseOrBlocksFromAndBlocks(andBlocks);
    List<Boolean> orRequired = new ArrayList<>();
    List<Boolean> andRequired = new ArrayList<>();
    setAndOrConditions(value, req, meta, andOrBlocks, orRequired, andRequired);

    int andTrueCount = 0;
    for (boolean and : andRequired) {
      if (and) {
        andTrueCount++;
      }
    }
    boolean orFoundTrue = false;
    for (boolean or : orRequired) {
      if (or) {
        orFoundTrue = true;
        break;
      }
    }
    return !(andTrueCount == andRequired.size() && orFoundTrue && value.length() == 0);
  }

  private void setAndOrConditions(String value, ServletRequest req, Metadata meta, List<String> andOrBlocks, List<Boolean> orRequired, List<Boolean> andRequired) {
    boolean nextIsAnd = false;
    boolean skipIteration = false;
    for (int i = 0; i < andOrBlocks.size(); i++) {
      if (skipIteration) {
        skipIteration = false;
        continue;
      }
      if (isRelatedBlockMakingChildRequired(andOrBlocks.get(i), value, req, meta)) {
        setAndOrCondition(orRequired, andRequired, nextIsAnd, true);
      } else {
        setAndOrCondition(orRequired, andRequired, nextIsAnd, false);
      }
      nextIsAnd = false;
      if (andOrBlocks.size() > i + 1) {
        if (andOrBlocks.get(i + 1).equals("AND")) {
          nextIsAnd = true;
        }
        skipIteration = true;
      }
    }
  }

  private void setAndOrCondition(List<Boolean> orRequired, List<Boolean> andRequired, boolean nextIsAnd, boolean value) {
    if (nextIsAnd) {
      andRequired.add(value);
    } else {
      orRequired.add(value);
    }
  }

  private List<String> parseOrBlocksFromAndBlocks(List<String> andBlocks) {
    List<String> andOrBlocks = new ArrayList<>();
    List<String> blocks;
    for (int i = 0; i < andBlocks.size(); i++) {
      blocks = parseBlocks(andBlocks.get(i), 0, "OR", ")||(", "(", ")");
      for (int j = 0; j < blocks.size(); j++) {
        if (blocks.get(j).equals("||")) {
          blocks.set(j, "OR");
        } else if (blocks.get(j).endsWith(")||")) {
          String block = blocks.get(j);
          blocks.set(j, block.substring(1, block.length() - 3));
          blocks.add(j + 1, "OR");
        }
      }
      andOrBlocks.addAll(blocks);
    }
    return andOrBlocks;
  }

  private boolean isRelatedBlockMakingChildRequired(String block, String value, ServletRequest req, Metadata meta) {
    String[] tagKeyValuePair = block.split(":");
    String parentValue = req.getParameter(tagKeyValuePair[0]);
    int parentLen = 0;
    if (parentValue != null) {
      parentLen = parentValue.length();
    }

    Item parentItem = meta.items.get(tagKeyValuePair[0]);

    if (tagKeyValuePair.length > 1) {
      if(tagKeyValuePair[1].equals("=")) {
        return isRelatedEqual(value, parentValue, parentLen, parentItem);
      }

      String[] ors = tagKeyValuePair[1].split("\\|\\|");
      for (String or : ors) {
        if (or.equals(parentValue)) {
          return true;
        }
      }
      return false;
    }

    System.out.println("no parent item found. parentLen: "+parentLen+", value.len="+value.length()+" returing: " + (parentLen > 0 && value.length() == 0));
    return parentLen > 0 && value.length() == 0;
  }

  private boolean isRelatedEqual(String value, String parentValue, int parentLen, Item parentItem) {
    if (value.length() > 0 && value.equals(parentValue)) {
      return false;
    } else {
      if (parentLen > 0) {
        return true;
      }
      return parentItem != null && parentItem.required && value.length() == 0;
    }
  }

  private List<String> parseBlocks(String s, int start, String andOr, String match, String reverseMatch, String forwardMatch) {
    List<String> blocks = new ArrayList<>();
    int lastPos = start;
    while (true) {
      int pos = s.indexOf(match, lastPos);
      if (pos > 0) {
        start = s.lastIndexOf(reverseMatch, pos);
        if (start != lastPos) {
          blocks.add(s.substring(lastPos, start));
        }
        blocks.add(s.substring(start + reverseMatch.length(), pos));
        blocks.add(andOr);
        int end = s.indexOf(forwardMatch, pos + match.length());
        blocks.add(s.substring(pos + match.length(), end));
        lastPos = end + forwardMatch.length();
      } else {
        if (lastPos + 1 < s.length()) {
          blocks.add(s.substring(lastPos, s.length()));
        }
        break;
      }
    }
    return blocks;
  }

}

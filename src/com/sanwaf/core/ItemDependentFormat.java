package com.sanwaf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

final class ItemDependentFormat extends Item {
  String depFormatString = null;
  String dependentElementName = null;
  Map<String,ItemFormat> formats = new HashMap<>();
  
  ItemDependentFormat(String name, String display, String type, int max, int min, String msg, String uri) {
    super(name, display, max, min, msg, uri);
    this.type = DEPENDENT_FORMAT;
    initDependentFormat(name, type, max, min, msg, uri);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value.length() == 0 || maskError.length() > 0) {
      return points;
    }
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    String elementValue = req.getParameter(dependentElementName);
    if(elementValue == null) {
      return false;
    }
    ItemFormat format = getFormatForValue(elementValue);
    return (format != null && format.inError(req, shield, value));
  }

  private ItemFormat getFormatForValue(String value) {
    Iterator<Map.Entry<String, ItemFormat>> it = formats.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry<String,ItemFormat> pair = it.next();
        if(value.equals(pair.getKey())){
          return pair.getValue();
        }
    }
    return null;
  }
  
  @Override
  String modifyErrorMsg(ServletRequest req, String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      String elementValue = req.getParameter(dependentElementName);
      ItemFormat format = getFormatForValue(elementValue);
      String formatString = " --- "; 
      if(format != null) {
        formatString = format.formatString;
      }
      return errorMsg.substring(0, i) + Metadata.jsonEncode(formatString) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void initDependentFormat(String name, String type, int max, int min, String msg, String uri) {
    int start = type.indexOf(DEPENDENT_FORMAT);
    if (start >= 0) {
      depFormatString = type.substring(start + DEPENDENT_FORMAT.length(), type.length() - 1);
      if(depFormatString.length() == 0) {
        return;
      }
      String[] elementFormatData = depFormatString.split(":");
      if(elementFormatData.length == 2) {
        dependentElementName = elementFormatData[0];
        String[] valueFormatPairs = elementFormatData[1].split(";");
        if(valueFormatPairs.length > 0) {
          parseFormats(name, display, max, min, msg, uri, valueFormatPairs);
        }
      }
    }
  }
  
  private void parseFormats(String name, String display, int max, int min, String msg, String uri, String[] valueFormatPairs) {
    for(String valueFormatPair : valueFormatPairs) {
      String[] kv = valueFormatPair.split("=");
      if(kv != null && kv.length == 2) {
        ItemFormat item = new ItemFormat(name, display, "f{" + kv[1] + "}", max, min, msg, uri);
        formats.put(kv[0], item);
      }
    }
  }
  
  void setAdditionalFields() {
    Iterator<Map.Entry<String, ItemFormat>> it = formats.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String,ItemFormat> pair = it.next();
      ItemFormat item = pair.getValue();
      item.required = required;
      item.maxValue = maxValue;
      item.minValue = minValue;
      item.related = related;
    }
  }
}

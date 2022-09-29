package com.sanwaf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

final class ItemDependentFormat extends Item {
  static final String INVALID_DEP_FORMAT = "Invalid Dependent Format: ";
  String depFormatString = null;
  String dependentElementName = null;
  Map<String,ItemFormat> formats = new HashMap<>();
  
  ItemDependentFormat(ItemData id) {
    super(id);
    initDependentFormat(id);
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
  boolean inError(final ServletRequest req, Shield shield, final String value) {
    if(mode == Modes.DISABLED) { return false; }
    String elementValue = req.getParameter(dependentElementName);
    if(elementValue == null) {
      return false;
    }
    ItemFormat format = getFormatForValue(elementValue);
    return handleMode((format != null && format.inError(req, shield, value)), value, INVALID_DEP_FORMAT + depFormatString, req);
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

  private void initDependentFormat(ItemData id) {
    int start = id.type.indexOf(ItemFactory.DEPENDENT_FORMAT);
    if (start >= 0) {
      depFormatString = id.type.substring(start + ItemFactory.DEPENDENT_FORMAT.length(), id.type.length() - 1);
      if(depFormatString.length() == 0) {
        return;
      }
      String[] elementFormatData = depFormatString.split(":");
      if(elementFormatData.length == 2) {
        dependentElementName = elementFormatData[0];
        String[] valueFormatPairs = elementFormatData[1].split(";");
        if(valueFormatPairs.length > 0) {
          parseFormats(id, valueFormatPairs);
        }
      }
    }
  }
  
  private void parseFormats(ItemData id, String[] valueFormatPairs) {
    for(String valueFormatPair : valueFormatPairs) {
      String[] kv = valueFormatPair.split("=");
      if(kv != null && kv.length == 2) {
        id.type = "f{" + kv[1] + "}";
        ItemFormat item = new ItemFormat(id);
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

  @Override 
  Types getType() {
    return Types.DEPENDENT_FORMAT;
  }
}

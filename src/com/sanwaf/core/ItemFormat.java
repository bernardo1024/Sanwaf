package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemFormat extends Item {
  String formatString = null;
  List<List<String>> formatsBlocks = new ArrayList<>();

  ItemFormat(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = FORMAT;
    setFormat(type);
  }

  @Override
  List<Point> getErrorPoints(final Shield shield, final String value) {
    List<Point> points = new ArrayList<>();
    if (value.length() == 0) {
      return points;
    }
    points.add(new Point(0, value.length()));
    return points;
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if(formatsBlocks.isEmpty()) {
      return false;
    }
    if (!required && value.length() == 0) {
      return false;
    }
    boolean foundValidFormat = false;
    for(List<String> formatBlocks : formatsBlocks) {
      if(!formatInError(value, formatBlocks)) {
        foundValidFormat = true;
        break;
      }
    }
    return !foundValidFormat;
  }

  private boolean formatInError(final String value, List<String> formatBlocks) {
    if(formatBlocks.isEmpty()) {
      return false;
    }

    int formatlen = formatBlocks.size();
    if (value.length() != formatlen) {
      return true;
    }
 
    for (int i = 0; i < value.length(); i++) {
      String c = String.valueOf(value.charAt(i));
      String formatBlock = formatBlocks.get(i);
      if(formatBlock.startsWith("#[")){
        formatBlock = formatBlock.substring(2, formatBlock.length() - 1);
        String[] maxMin = formatBlock.split("-");
        if(maxMin.length != 2) {
          return false;
        }
      
        int minNum = 0;
        int maxNum = 0;
        int maxLen = 0;

        try {
          minNum = Integer.parseInt(maxMin[0]);
          maxNum = Integer.parseInt(maxMin[1]);
          maxLen = (maxNum + "").length();
        }catch(NumberFormatException e) {
          return false;
        }
 
        if(c.charAt(0) < '0' || c.charAt(0) > '9'){
          return true;
        }
 
        StringBuilder cBlock = new StringBuilder(c);
        if(maxLen > 1){
          for(int j = 1; j < maxLen; j++){
            char n = value.charAt(i + j);
            if(n >= '0' && n <= '9'){
              cBlock.append(n);
            }
            else {
              return true;
            }
          }
        }
        if(Integer.parseInt(cBlock.toString()) >= minNum && Integer.parseInt(cBlock.toString()) <= maxNum){
          i += maxLen - 1;
        }
        else{
          return true;
        }
      }
      else{
        char cF = formatBlock.charAt(0);
        char cC = c.charAt(0);
        if ((cF == '#' && cC >= '0' && cC <= '9') || 
            ((cF == 'A' || cF == 'c') && cC >= 'A' && cC <= 'Z') || 
            ((cF == 'a' || cF == 'c')  && cC >= 'a' && cC <= 'z') ) {
          continue;
        }
        if (cC != unEscapedChar(cF)) {
          return true;
        }
      }
    }
    return false;
  }

  private String escapeChars(String s){
    s = s.replaceAll("\\\\#", "\t");
    s = s.replaceAll("\\\\A", "\n");
    s = s.replaceAll("\\\\a", "\r");
    s = s.replaceAll("\\\\c", "\f");
    s = s.replaceAll("\\\\\\[", "\b");
    s = s.replaceAll("\\\\\\]", "\0");
    return s;
  }

  private char unEscapedChar(char c){
    if(c == '\t') {
      return '#';
    } else if (c == '\n') {
      return 'A';
    } else if (c == '\r') {
      return 'a';
    } else if (c == '\f') {
      return 'c';
    } else if (c == '\b') {
      return '[';
    } else if (c == '\0') {
      return ']';
    }
    return c;
  }

  @Override
  String modifyErrorMsg(String errorMsg) {
    int i = errorMsg.indexOf(Error.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(formatString) + errorMsg.substring(i + Error.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void setFormat(String value) {
    int start = value.indexOf(FORMAT);
    if (start >= 0) {
      formatString = value.substring(start + FORMAT.length(), value.length() - 1);
      parseFormats(formatString);
    }
  }
  
  
  private void parseFormats(String format) {
    if(format.length() == 0) {
      return;
    }
    String[] formats = format.split("\\|\\|");
    
    for(String thisFormat : formats) {
      formatsBlocks.add(parseFormat(thisFormat));
    }
  }
  
  
  private List<String> parseFormat(String format){
    List<String> formatBlocks = new ArrayList<>();
    format = escapeChars(format);
    int pos = 0;
    int last = 0;
    int end = 0;
    int dash = 0;
    
    while(true){
      String block = "";
      int numDigits = 0;
      pos = format.indexOf('#', last); 
      if(pos < 0){
        addRemainderCharsAsBlocks(format, last, formatBlocks);
        break;
      }
      if(format.length() > pos + 1 && format.charAt(pos + 1) == '['){
        dash = format.indexOf('-', pos);
        if(dash > 0) {
          end = format.indexOf(']', pos);
          if(end > 0) {
            numDigits = end - (dash + 1);
            block = format.substring(last, end + 1);
            last = end + 1;
          }
        }
      }
      else{
        block = format.substring(last, pos + 1);
        last = pos + 1;
      }
      
      block = addStartingCharsAsBlocks(block, formatBlocks);
      if(block == null) {
        formatBlocks = new ArrayList<>();
        break;
      }
      formatBlocks.add(block);
      addPlaceholderBlocks(numDigits, formatBlocks);
    }
    return formatBlocks;
  }

  private void addPlaceholderBlocks(int numDigits, List<String> formatBlocks) {
    for(int i = 0; i < numDigits - 1; i++){
      formatBlocks.add("");
    }
  }

  private String addStartingCharsAsBlocks(String block, List<String> formatBlocks) {
    if(!block.startsWith("#")){
      int x = block.indexOf('#');
      if(x < 0) {
        return null;
      }
      String s = block.substring(0, x);
      formatBlocks.addAll(Arrays.asList(s.split("")));
      block = block.substring(x, block.length());
    }
    return block;
  }

  private void addRemainderCharsAsBlocks(String format, int last, List<String> formatBlocks) {
    if(last < format.length()) {
      formatBlocks.addAll(Arrays.asList(format.substring(last, format.length()).split("")));
    }
  }
  
}

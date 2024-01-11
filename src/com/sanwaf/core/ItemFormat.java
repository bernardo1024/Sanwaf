package com.sanwaf.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import jakarta.servlet.ServletRequest;

final class ItemFormat extends Item {
  static final String INVALID_FORMAT = "Invalid Format: ";
  String formatString = null;
  List<List<String>> formatsBlocks = new ArrayList<>();

  ItemFormat(ItemData id) {
    super(id);
    setFormat(id.type);
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
  boolean inError(final ServletRequest req, final Shield shield, final String value, boolean doAllBlocks, boolean log) {
    if (mode == Modes.DISABLED) {
      return false;
    }
    if (formatsBlocks.isEmpty()) {
      return false;
    }
    if (!required && value.length() == 0) {
      return false;
    }
    boolean foundValidFormat = false;
    for (List<String> formatBlocks : formatsBlocks) {
      if (!formatInError(value, formatBlocks)) {
        foundValidFormat = true;
        break;
      }
    }

    if(!foundValidFormat) {
      return true;
    }
    return false;
  }

  private boolean formatInError(final String value, List<String> formatBlocks) {
    if (formatBlocks.isEmpty()) {
      return false;
    }

    int formatlen = formatBlocks.size();
    if (!(formatString.contains("dd") || formatString.contains("mm") || formatString.contains("yy") || formatString.contains("yyyy)")) && value.length() != formatlen) {
      return true;
    }

    for (int i = 0; i < value.length(); i++) {
      String c = String.valueOf(value.charAt(i));
      String formatBlock = formatBlocks.get(i);

      formatBlock = resolveDateVariables(formatBlock);

      if (formatBlock.startsWith("#[")) {
        formatBlock = formatBlock.substring(2, formatBlock.length() - 1);

        if (formatBlock.contains(",")) {
          List<String> validNums = Arrays.asList(formatBlock.split(","));
          if (!validNums.contains(c)) {
            return true;
          }
        } else {
          String[] maxMin = formatBlock.split("-");
          if (maxMin.length != 2) {
            return false;
          }

          int minNum = 0;
          int maxNum = 0;
          int maxLen = 0;
          try {
            minNum = Integer.parseInt(maxMin[0]);
            maxNum = Integer.parseInt(maxMin[1]);
            maxLen = (maxNum + "").length();
          } catch (NumberFormatException e) {
            return false;
          }

          if (c.charAt(0) < '0' || c.charAt(0) > '9') {
            return true;
          }

          StringBuilder cBlock = new StringBuilder(c);
          if (maxLen > 1) {
            for (int j = 1; j < maxLen; j++) {
              if (i + j <= value.length() - 1) {
                char n = value.charAt(i + j);
                if (n >= '0' && n <= '9') {
                  cBlock.append(n);
                } else {
                  return true;
                }
              }
            }
          }
          if (Integer.parseInt(cBlock.toString()) >= minNum && Integer.parseInt(cBlock.toString()) <= maxNum) {
            i += maxLen - 1;
          } else {
            return true;
          }
        }
      } else {
        char cF = formatBlock.charAt(0);
        char cC = c.charAt(0);
        if ((cF == 'x') || (cF == '#' && cC >= '0' && cC <= '9') || ((cF == 'A' || cF == 'c') && cC >= 'A' && cC <= 'Z') || ((cF == 'a' || cF == 'c') && cC >= 'a' && cC <= 'z')) {
          continue;
        }
        if (cC != unEscapedChar(cF)) {
          return true;
        }
      }
    }
    return false;
  }

  private String resolveDateVariables(String format) {
    String newMdy = "";
    String parsedValue = format;
    String[] dateOrder = { "dd", "mm", "yyyy", "yy" };

    for (int i = 0; i < dateOrder.length; i++) {
      int last = 0;
      while (true) {
        int startMdyReplacePos = 0;
        int endMdyReplacePos = 0;
        last = parsedValue.indexOf(dateOrder[i]);
        if (last < 0) {
          break;
        }
        startMdyReplacePos = last;
        endMdyReplacePos = last + dateOrder[i].length();

        if (dateOrder[i].equals("yy")) {
          int year = Calendar.getInstance().get(Calendar.YEAR);
          newMdy = String.valueOf(year).substring(2);
          last += 2;
          newMdy = adjustDate(parsedValue, last, newMdy);
        } else if (dateOrder[i].equals("yyyy")) {
          int year = Calendar.getInstance().get(Calendar.YEAR);
          newMdy = String.valueOf(year);
          last += 4;
          newMdy = adjustDate(parsedValue, last, newMdy);
        } else if (dateOrder[i].equals("mm")) {
          int month = Calendar.getInstance().get(Calendar.MONTH);
          newMdy = String.valueOf(month + 1);
          last += 2;
          newMdy = adjustDate(parsedValue, last, newMdy);
          if (Integer.parseInt(newMdy) > 12) {
            newMdy = "12";
          }
        } else if (dateOrder[i].equals("dd")) {
          int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
          newMdy = String.valueOf(day);
          last += 2;
          newMdy = adjustDate(parsedValue, last, newMdy);
          if (Integer.parseInt(newMdy) > 31) {
            newMdy = "31";
          }
        }

        if (parsedValue.substring(last, last + 1).equals("(")) {
          int endOfNum = parsedValue.indexOf(')', last);
          parsedValue = parsedValue.substring(0, startMdyReplacePos) + newMdy + parsedValue.substring(endOfNum + 1, parsedValue.length());
        } else {
          parsedValue = parsedValue.substring(0, startMdyReplacePos) + newMdy + parsedValue.substring(endMdyReplacePos, parsedValue.length());
        }
      }
    }
    return parsedValue;
  }

  private String adjustDate(String parsedValue, int last, String newMdy) {
    int newValue = Integer.parseInt(newMdy);
    if (parsedValue.substring(last, last + 1).equals("(")) {
      int endOfNum = parsedValue.indexOf(')', last);
      String num = parsedValue.substring(last + 2, endOfNum);
      int parsedNum = Integer.parseInt(num);
      String arith = parsedValue.substring(last + 1, last + 2);
      if (arith.equals("+")) {
        newValue += parsedNum;
      } else if (arith.equals("-")) {
        newValue -= parsedNum;
      }
    }
    return String.valueOf(newValue);
  }

  private String escapeChars(String s) {
    s = s.replaceAll("\\\\#", "\t");
    s = s.replaceAll("\\\\A", "\n");
    s = s.replaceAll("\\\\a", "\r");
    s = s.replaceAll("\\\\c", "\f");
    s = s.replaceAll("\\\\\\[", "\b");
    s = s.replaceAll("\\\\\\]", "\0");
    s = s.replaceAll("\\\\\\|", "\1");
    s = s.replaceAll("\\\\x", "\2");
    s = s.replaceAll("\\\\:", "\3");
    s = s.replaceAll("\\\\=", "\4");
    s = s.replaceAll("\\\\\\(", "\5");
    s = s.replaceAll("\\\\\\)", "\6");
    s = s.replaceAll("\\\\\\+", "\7");
    s = s.replaceAll("\\\\\\-", "\016");
    s = s.replaceAll("\\\\;", "\017");

    return s;
  }

  private char unEscapedChar(char c) {
    switch (c) {
    case '\t':
      return '#';
    case '\n':
      return 'A';
    case '\r':
      return 'a';
    case '\f':
      return 'c';
    case '\b':
      return '[';
    case '\0':
      return ']';
    case '\1':
      return '|';
    case '\2':
      return 'x';
    case '\3':
      return ':';
    case '\4':
      return '=';
    case '\5':
      return '(';
    case '\6':
      return ')';
    case '\7':
      return '+';
    case '\016':
      return '-';
    case '\017':
      return ';';
    default:
      return c;
    }
  }

  @Override
  String modifyErrorMsg(ServletRequest req, String errorMsg) {
    int i = errorMsg.indexOf(ItemFactory.XML_ERROR_MSG_PLACEHOLDER1);
    if (i >= 0) {
      return errorMsg.substring(0, i) + Metadata.jsonEncode(formatString) + errorMsg.substring(i + ItemFactory.XML_ERROR_MSG_PLACEHOLDER1.length(), errorMsg.length());
    }
    return errorMsg;
  }

  private void setFormat(String value) {
    int start = value.indexOf(ItemFactory.FORMAT);
    if (start >= 0) {
      formatString = value.substring(start + ItemFactory.FORMAT.length(), value.length() - 1);
      parseFormats(formatString);
    }
  }

  private void parseFormats(String format) {
    if (format.length() == 0) {
      return;
    }
    String[] formats = format.split("\\|\\|");

    for (String thisFormat : formats) {
      formatsBlocks.add(parseFormat(thisFormat));
    }
  }

  private List<String> parseFormat(String format) {
    List<String> formatBlocks = new ArrayList<>();
    format = escapeChars(format);
    int pos = 0;
    int last = 0;
    int end = 0;
    int dash = 0;

    while (true) {
      String block = "";
      int numDigits = 0;
      pos = format.indexOf('#', last);
      if (pos < 0) {
        addRemainderCharsAsBlocks(format, last, formatBlocks);
        break;
      }
      if (format.length() > pos + 1 && format.charAt(pos + 1) == '[') {
        end = format.indexOf(']', pos);
        if (format.contains(",")) {
          numDigits = 0;
        } else {
          dash = format.indexOf('-', pos);
          if (dash > 0 && end > 0) {
            numDigits = end - (dash + 1);
          }
        }
        block = format.substring(last, end + 1);
        last = end + 1;
      } else {
        block = format.substring(last, pos + 1);
        last = pos + 1;
      }

      block = addStartingCharsAsBlocks(block, formatBlocks);
      if (block == null) {
        formatBlocks = new ArrayList<>();
        break;
      }
      formatBlocks.add(block);
      addPlaceholderBlocks(numDigits, formatBlocks);
    }
    return formatBlocks;
  }

  private void addPlaceholderBlocks(int numDigits, List<String> formatBlocks) {
    for (int i = 0; i < numDigits - 1; i++) {
      formatBlocks.add("");
    }
  }

  private String addStartingCharsAsBlocks(String block, List<String> formatBlocks) {
    if (!block.startsWith("#")) {
      int x = block.indexOf('#');
      if (x < 0) {
        return null;
      }
      String s = block.substring(0, x);
      formatBlocks.addAll(Arrays.asList(s.split("")));
      block = block.substring(x, block.length());
    }
    return block;
  }

  private void addRemainderCharsAsBlocks(String format, int last, List<String> formatBlocks) {
    if (last < format.length()) {
      formatBlocks.addAll(Arrays.asList(format.substring(last, format.length()).split("")));
    }
  }

  @Override
  String getProperties() {
    return "\"format\":\"" + Metadata.jsonEncode(formatString) + "\"";
  }

  @Override
  Types getType() {
    return Types.FORMAT;
  }
}


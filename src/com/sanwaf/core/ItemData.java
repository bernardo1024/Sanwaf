package com.sanwaf.core;

class ItemData{
  String name;
  String display;
  String type;
  int min;
  int max;
  String msg;
  String uri;
  String sMode;
  
  ItemData(String name, String sMode, String display, String type, String msg, String uri, int max, int min){
    this.name = name; 
    this.display = display; 
    this.type = type;
    this.min = min;
    this.max = max; 
    this.msg = msg;
    this.uri = uri;
    this.sMode = sMode;
  }

  static Modes getMode(String sMode, Modes def) {
    switch(sMode.toLowerCase()) {
    case "disabled":
      return Modes.DISABLED;
    case "block":
      return Modes.BLOCK;
    case "detect":
      return Modes.DETECT;
    case "detect_all":
      return Modes.DETECT_ALL;
    default:
      return def;
    }
  } 
}
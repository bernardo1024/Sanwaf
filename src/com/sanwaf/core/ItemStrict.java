package com.sanwaf.core;

import java.util.List;

import javax.servlet.ServletRequest;

public class ItemStrict extends Item {

  ItemStrict(String s) {
	  msg = s;
  }
  
  @Override
  boolean inError(ServletRequest req, Shield shield, String value, boolean doAllBlocks, boolean log) {
    return false;
  }

  @Override
  List<Point> getErrorPoints(Shield shield, String value) {
    return null;
  }

  @Override
  Types getType() {
    return null;
  }

}

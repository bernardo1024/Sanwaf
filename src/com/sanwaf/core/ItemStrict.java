package com.sanwaf.core;

import java.util.List;

import jakarta.servlet.ServletRequest;

public class ItemStrict extends Item {

  ItemStrict(String s) {
    
  }
  
  @Override
  boolean inError(ServletRequest req, Shield shield, String value, boolean doAllBlocks) {
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

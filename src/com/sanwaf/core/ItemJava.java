package com.sanwaf.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemJava extends Item {
  Method javaMethod = null;

  ItemJava(ItemData id) {
    super(id);
    setJavaMethod(id.type);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return handleMode(true, value);
    }
    if (isSizeError(value)) {
      return handleMode(true, value);
    }
    if(value.length() == 0) {
      return false;
    }
    return handleMode(runJavaMethod(javaMethod, value, req), value);
  }

  @Override
  List<Point> getErrorPoints(Shield shield, String value) {
    List<Point> points = new ArrayList<>();
    if(maskError.length() > 0) {
      return points;
    }
    points.add(new Point(0, value.length()));
    return points;
  }

  private void setJavaMethod(String type) {
    String sClazzAndMethod = type.substring(type.indexOf(ItemFactory.JAVA) + ItemFactory.JAVA.length(), type.length() - 1);
    if (sClazzAndMethod.length() == 0) {
      return;
    }

    Class<?> clazz = null;
    try {
      clazz = Class.forName(parseClazz(sClazzAndMethod));
      javaMethod = clazz.getMethod(parseMethod(sClazzAndMethod), String.class, ServletRequest.class);
    } catch (ClassNotFoundException | NullPointerException | NoSuchMethodException e) {
      javaMethod = null;
    }
  }

  static String parseClazz(String s) {
    int last = s.lastIndexOf('.');
    if (last > 0) {
      return s.substring(0, last);
    }
    return s;
  }

  static String parseMethod(String s) {
    int start = s.lastIndexOf('.');
    if (start > 0) {
      int end = s.lastIndexOf('(');
      if (end > 0) {
        return s.substring(start + 1, end);
      }
    }
    return s;
  }

  static boolean runJavaMethod(Method method, String v, ServletRequest req) {
    try {
      Object o = method.invoke(null, v, req);
      return Boolean.valueOf(String.valueOf(o));
    } catch (NullPointerException | IllegalAccessException | InvocationTargetException e) {
      return true;
    }
  }

  @Override 
  Types getType() {
    return Types.JAVA;
  }
}

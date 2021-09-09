package com.sanwaf.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

final class ItemJava extends Item {
  Method javaMethod = null;

  ItemJava(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = JAVA;
    setJavaMethod(type);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if (!isUriValid(req)) {
      return true;
    }
    if (isSizeError(value)) {
      return true;
    }
    if(value.length() == 0) {
      return false;
    }
    return runJavaMethod(javaMethod, value, req);
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
    String sClazzAndMethod = type.substring(type.indexOf(JAVA) + JAVA.length(), type.length() - 1);
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
}

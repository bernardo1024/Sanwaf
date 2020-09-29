package com.sanwaf.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;

final class ParameterJava extends Parameter {
  Method javaMethod = null;

  ParameterJava(String name, String type, int max, int min, String msg, String uri) {
    super(name, max, min, msg, uri);
    this.type = Metadata.TYPE_JAVA;
    parseJavaMethod(type);
  }

  @Override
  boolean inError(final ServletRequest req, final Shield shield, final String value) {
    if(!isPathValid(req)) { return false; }
    if(isSizeError(value)) { return true; }
    return runJavaMethod(javaMethod, value, req);
  }

  private void parseJavaMethod(String type) {
    String sClazzAndMethod = type.substring(type.indexOf(Metadata.TYPE_JAVA) + Metadata.TYPE_JAVA.length(), type.length() - 1);
    if (sClazzAndMethod == null || sClazzAndMethod.length() == 0) {
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

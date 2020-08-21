package com.sanwaf.log;

//DO NOT USE THIS CLASS IN PRODUCTION
public final class LoggerSystemOut implements Logger {

  @Override
  public void error(String s) {
    System.out.println("Sanwaf-error:\t" + s);
  }

  @Override
  public void info(String s) {
    System.out.println("Sanwaf-info:\t" + s);
  }

}

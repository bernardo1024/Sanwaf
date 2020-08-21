package com.sanwaf.core;

final class Parameter {
  Datatype type;
  int max;
  int min;

  Parameter(Datatype type) {
    max = Integer.MAX_VALUE;
    min = 0;
    this.type = type;
  }

  Parameter(Datatype type, int max, int min) {
    this.type = type;
    this.max = max;
    this.min = min;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("type: ").append(type);
    sb.append(", max: ").append(max);
    sb.append(", min: ").append(min);
    return sb.toString();
  }
}

package com.sanwaf.core;

//used to return true/false/null
class ModeError {
  boolean error = false;
  boolean isSize = false;
  boolean isUri = false;
  
  ModeError(boolean error){
    this.error = error;
  }
}

package com.imgtec.creator.sniffles.data.utils;


public class Condition {

  public static void check(boolean condition, String errorMessage, Object... messageArgs) {
    if (condition == false) {
      throw new IllegalStateException(String.format(errorMessage, messageArgs));
    }
  }
}
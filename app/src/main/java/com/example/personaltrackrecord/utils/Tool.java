package com.example.personaltrackrecord.utils;

public class Tool {
  public static String getPrintString(boolean needBreakLine, Object... args) {
    String printString = "";
    for (Object s : args) {
      printString += needBreakLine ? s.toString() + "\n" : s.toString() + "    ";
    }
    return printString;
  }

  public static void printString(boolean needBreakLine, Object... args) {
    String printString = "";
    for (Object s : args) {
      printString += needBreakLine ? s.toString() + "\n" : s.toString() + "    ";
    }
    System.out.println(printString);
  }
}

package com.derpgroup.quip.util;

import java.util.Map;
import java.util.Map.Entry;

public class QuipUtil {

  public static String substituteContent(String input, Map<String,String> replacementValues) {
    String modifiedInput = input;
    for(Entry<String, String> entry : replacementValues.entrySet()){
      modifiedInput = modifiedInput.replace(entry.getKey(), entry.getValue());
    }
    return modifiedInput;
  }
}

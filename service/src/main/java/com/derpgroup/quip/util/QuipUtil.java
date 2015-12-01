package com.derpgroup.quip.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class QuipUtil {

  public static String substituteBracketedContent(String input, Map<String,String> replacementValues){
    Map<String,String> bracketedReplacementValues = new HashMap<String, String>();
    for(Entry<String, String> entry : replacementValues.entrySet()){
      bracketedReplacementValues.put("["+entry.getKey()+"]", entry.getValue());
    }
    return substituteContent(input, bracketedReplacementValues);
  }

  public static String substituteContent(String input, Map<String,String> replacementValues) {
    String modifiedInput = input;
    for(Entry<String, String> entry : replacementValues.entrySet()){
      modifiedInput = modifiedInput.replace(entry.getKey(), entry.getValue());
    }
    return modifiedInput;
  }
}

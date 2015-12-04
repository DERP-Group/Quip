package com.derpgroup.quip.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.derpgroup.quip.model.Quip;

public class QuipUtil {

  /**
   * Returns a new string that contains the substituted values provided.
   * @param input
   * @param replacementValues
   * @return
   */
  public static String substituteContent(String input, Map<String,String> replacementValues) {
    if(replacementValues==null || StringUtils.isEmpty(input)){return input;}
    String modifiedInput = input;
    for(Entry<String, String> entry : replacementValues.entrySet()){
      modifiedInput = modifiedInput.replace(entry.getKey(), entry.getValue());
    }
    return modifiedInput;
  }
  
  /**
   * Builds a new quip with the content replaced with the substituted values.
   * @param quip
   * @param phoneticReplacements
   * @param normalReplacements
   * @return
   */
  public static Quip substituteContent(Quip quip, Map<String,String> phoneticReplacements, Map<String,String> normalReplacements){
    Quip newQuip = new Quip();
    newQuip.setQuipGroup(quip.getQuipGroup());
    newQuip.setTags(quip.getTags());
    newQuip.setUsageRules(quip.getUsageRules());
    newQuip.setText(substituteContent(quip.getText(),normalReplacements));
    newQuip.setTargetableText(substituteContent(quip.getTargetableText(),normalReplacements));
    
    String phoneticSubbedSsml = substituteContent(quip.getSsml(),phoneticReplacements);
    newQuip.setSsml(substituteContent(phoneticSubbedSsml,normalReplacements));
    
    String phoneticSubbedTargetedSsml = substituteContent(quip.getTargetableSsml(),phoneticReplacements);
    newQuip.setTargetableSsml(substituteContent(phoneticSubbedTargetedSsml,normalReplacements));
    return newQuip;
  }
}

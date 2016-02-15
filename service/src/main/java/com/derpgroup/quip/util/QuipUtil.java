package com.derpgroup.quip.util;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;
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
  
  /**
   * An helper function to map Alexa specific "intents" into Quip specific subjects.
   * @param request
   * @return
   */
  public static String getMessageSubject(SpeechletRequest request) {
    if(request instanceof LaunchRequest){
      return "START_OF_CONVERSATION";
    }else if(request instanceof SessionEndedRequest){
      return "END_OF_CONVERSATION";
    }else if (!(request instanceof IntentRequest)) {
      return "";
    }
    
    IntentRequest intentRequest = (IntentRequest) request;
    String intentRequestName = intentRequest.getIntent().getName();
    if(intentRequestName.equalsIgnoreCase("AMAZON.HelpIntent")){
      return "HELP";
    }
    if(intentRequestName.equalsIgnoreCase("AMAZON.CancelIntent")){
      return "CANCEL";
    }
    if(intentRequestName.equalsIgnoreCase("AMAZON.StopIntent")){
      return "STOP";
    }
    if(intentRequestName.equalsIgnoreCase("AMAZON.YesIntent")){
      return "YES";
    }
    if(intentRequestName.equalsIgnoreCase("AMAZON.NoIntent")){
      return "NO";
    }
    if(intentRequestName.equalsIgnoreCase("AMAZON.RepeatIntent")){
      return "REPEAT";
    }
    return intentRequestName;
  }
}

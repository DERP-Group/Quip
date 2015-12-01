package com.derpgroup.quip.util;

import java.util.Map;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;

public class QuipUtil {

  public static String substituteContent(String input, Map<String,String> replacementValues) throws DerpwizardException{

    String currentInput = input;
    int firstBrace = currentInput.indexOf("[");
    int secondBrace = currentInput.indexOf("]");
    while(firstBrace != -1 || secondBrace != -1){
      if(firstBrace == -1){
        if(secondBrace == -1){
          break;
        }
        else{
          throw new DerpwizardException("The content of the response was malformed.", "Missing opening bracket detected in content: "+currentInput, "The content of the response was malformed.");
        }
      }
      else{
        if(secondBrace == -1){
          throw new DerpwizardException("The content of the response was malformed.", "Missing closing bracket detected in content: "+currentInput, "The content of the response was malformed.");
        }
        else{
          String contentsWithinBrackets = currentInput.substring(firstBrace+1, secondBrace);
          if(replacementValues.containsKey(contentsWithinBrackets)){
             currentInput = currentInput.substring(0, firstBrace) + replacementValues.get(contentsWithinBrackets) + currentInput.substring(secondBrace+1);
          }
          else{
            throw new DerpwizardException("The content of the response was malformed.", "Unknown substitution value of "+contentsWithinBrackets+" was detected in content: "+currentInput, "The content of the response was malformed.");
          }

          firstBrace = currentInput.indexOf("[");
          secondBrace = currentInput.indexOf("]");
        }
      }
    }
    return currentInput;
  }
}

package com.derpgroup.quip.logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.quip.QuipMetadata;

public class QuipLogger {
  private static final Logger LOG = LoggerFactory.getLogger(QuipLogger.class);
  
  public static void log(VoiceInput voiceInput){
    String intent = voiceInput.getMessageSubject();
    if(intent.equals("ANOTHER")){return;}
    
    QuipMetadata metadata = (QuipMetadata)voiceInput.getMetadata();
    String userId = metadata.getUserId();

    int botNumber = -1;
    switch(metadata.getBot()){
    case "complibot":
      botNumber = 0;
      break;
    case "insultibot":
      botNumber = 1;
      break;
    default:
      botNumber = -1;
      break;
    }
    
    Deque<ConversationHistoryEntry> conversationHistory = metadata.getConversationHistory();
    int conversationHistorySize = 0;
    if(conversationHistory!=null){
      conversationHistorySize = conversationHistory.size();
    }
    
    // Alphabetically order the key/values for consistency
    Map<String, String> messageMap = voiceInput.getMessageAsMap();
    List<String> orderedKeys = new ArrayList<String>();
    orderedKeys.addAll(messageMap.keySet());
    Collections.sort(orderedKeys);
    
    String orderedKeyValuePairs = "";
    for(String key: orderedKeys){
      orderedKeyValuePairs+=","+key+"="+messageMap.get(key);
    }
    
    LOG.info( botNumber+ "," + intent + ","+userId+","+conversationHistorySize+orderedKeyValuePairs); 
  }
  
  public static void logAnother(VoiceInput voiceInput){
    QuipMetadata metadata = (QuipMetadata)voiceInput.getMetadata();
    String userId = metadata.getUserId();

    int botNumber = -1;
    switch(metadata.getBot()){
    case "complibot":
      botNumber = 0;
      break;
    case "insultibot":
      botNumber = 1;
      break;
    default:
      botNumber = -1;
      break;
    }
    
    Deque<ConversationHistoryEntry> conversationHistory = metadata.getConversationHistory();
    int conversationHistorySize = 1;
    if(conversationHistory!=null){
      conversationHistorySize = conversationHistory.size()+1;
    }
    
    String intent = voiceInput.getMessageSubject();
    if(intent.equals("ANOTHER")){
      conversationHistorySize = 0;
      switch(metadata.getBot()){
      case "complibot":
        intent = "COMPLIMENT";
        break;
      case "insultibot":
        intent = "INSULT";
        break;
      default:
        intent = "UNKNOWN";
        break;
      }
    }
    
    // Alphabetically order the key/values for consistency
    Map<String, String> messageMap = voiceInput.getMessageAsMap();
    List<String> orderedKeys = new ArrayList<String>();
    orderedKeys.addAll(messageMap.keySet());
    Collections.sort(orderedKeys);
    
    String orderedKeyValuePairs = "";
    for(String key: orderedKeys){
      orderedKeyValuePairs+=","+key+"="+messageMap.get(key);
    }
    
    LOG.info( botNumber+ ",ANOTHER="+ intent + ","+userId+","+conversationHistorySize+orderedKeyValuePairs); 
  }
}

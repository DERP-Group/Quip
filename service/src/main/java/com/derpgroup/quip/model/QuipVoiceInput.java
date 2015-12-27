package com.derpgroup.quip.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.derpgroup.derpwizard.voice.model.VoiceInput;

public class QuipVoiceInput implements VoiceInput{
  
  private Map<String,String> messageMap = new LinkedHashMap<String,String>();
  private CommonMetadata metadata;
  private String messageSubject;
  
  public QuipVoiceInput(){};
  
  public void setMessageMap(Map<String, String> messageMap) {this.messageMap = messageMap;}
  public void setMetadata(CommonMetadata metadata) {this.metadata = metadata;}
  public void setMessageSubject(String messageSubject) {this.messageSubject = messageSubject;}
  
  @Override
  public @NonNull String getMessageSubject() {
    return messageSubject;
  }

  @Override
  public @NonNull Map<String, String> getMessageAsMap() {
    return messageMap;
  }

  @Override
  public @NonNull CommonMetadata getMetadata() {
    return metadata;
  }

  @Override
  public @NonNull MessageType getMessageType() {
    return MessageType.DEFAULT;
  }

}

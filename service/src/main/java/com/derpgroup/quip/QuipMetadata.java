package com.derpgroup.quip;

import java.util.ArrayDeque;
import java.util.Deque;

import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property="type", defaultImpl = QuipMetadata.class)
public class QuipMetadata extends CommonMetadata {
  
  private String bot;
  private Deque<String> insultsUsed;
  private Deque<String> complimentsUsed;
  private Deque<String> winsultsUsed;
  private Deque<String> backhandedComplimentsUsed;

  public String getBot() {
    return bot;
  }

  public void setBot(String bot) {
    this.bot = bot;
  }

  public Deque<String> getInsultsUsed() {
    if(insultsUsed == null){
      insultsUsed = new ArrayDeque<String>();
    }
    return insultsUsed;
  }

  public void setInsultsUsed(Deque<String> insultsUsed) {
    this.insultsUsed = insultsUsed;
  }

  public Deque<String> getComplimentsUsed() {
    if(complimentsUsed == null){
      complimentsUsed = new ArrayDeque<String>();
    }
    return complimentsUsed;
  }

  public void setComplimentsUsed(Deque<String> complimentsUsed) {
    this.complimentsUsed = complimentsUsed;
  }

  public Deque<String> getWinsultsUsed() {
    if(winsultsUsed == null){
      winsultsUsed = new ArrayDeque<String>();
    }
    return winsultsUsed;
  }

  public void setWinsultsUsed(Deque<String> winsultsUsed) {
    this.winsultsUsed = winsultsUsed;
  }

  public Deque<String> getBackhandedComplimentsUsed() {
    if(backhandedComplimentsUsed == null){
      backhandedComplimentsUsed = new ArrayDeque<String>();
    }
    return backhandedComplimentsUsed;
  }

  public void setBackhandedComplimentsUsed(Deque<String> backhandedComplimentsUsed) {
    this.backhandedComplimentsUsed = backhandedComplimentsUsed;
  }
}

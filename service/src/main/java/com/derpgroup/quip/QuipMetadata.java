package com.derpgroup.quip;

import java.util.LinkedList;
import java.util.Queue;

import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property="type", defaultImpl = QuipMetadata.class)
public class QuipMetadata extends CommonMetadata {
  
  private String bot;
  private Queue<String> insultsUsed;
  private Queue<String> complimentsUsed;
  private Queue<String> winsultsUsed;
  private Queue<String> backhandedComplimentsUsed;
  private String userId;

  public String getBot() {
    return bot;
  }

  public void setBot(String bot) {
    this.bot = bot;
  }

  public Queue<String> getInsultsUsed() {
    if(insultsUsed == null){
      insultsUsed = new LinkedList<String>();
    }
    return insultsUsed;
  }

  public void setInsultsUsed(Queue<String> insultsUsed) {
    this.insultsUsed = insultsUsed;
  }

  public Queue<String> getComplimentsUsed() {
    if(complimentsUsed == null){
      complimentsUsed = new LinkedList<String>();
    }
    return complimentsUsed;
  }

  public void setComplimentsUsed(Queue<String> complimentsUsed) {
    this.complimentsUsed = complimentsUsed;
  }

  public Queue<String> getWinsultsUsed() {
    if(winsultsUsed == null){
      winsultsUsed = new LinkedList<String>();
    }
    return winsultsUsed;
  }

  public void setWinsultsUsed(Queue<String> winsultsUsed) {
    this.winsultsUsed = winsultsUsed;
  }

  public Queue<String> getBackhandedComplimentsUsed() {
    if(backhandedComplimentsUsed == null){
      backhandedComplimentsUsed = new LinkedList<String>();
    }
    return backhandedComplimentsUsed;
  }

  public void setBackhandedComplimentsUsed(Queue<String> backhandedComplimentsUsed) {
    this.backhandedComplimentsUsed = backhandedComplimentsUsed;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}

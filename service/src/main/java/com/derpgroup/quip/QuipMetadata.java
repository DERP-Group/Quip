package com.derpgroup.quip;

import java.util.ArrayList;
import java.util.List;

import com.derpgroup.derpwizard.voice.model.CommonMetadata;

public class QuipMetadata extends CommonMetadata {
  
  private String bot;
  private List<String> insultsUsed;
  private List<String> complimentsUsed;
  private List<String> winsultsUsed;
  private List<String> backhandedComplimentsUsed;

  public String getBot() {
    return bot;
  }

  public void setBot(String bot) {
    this.bot = bot;
  }

  public List<String> getInsultsUsed() {
    if(insultsUsed == null){
      insultsUsed = new ArrayList<String>();
    }
    return insultsUsed;
  }

  public void setInsultsUsed(List<String> insultsUsed) {
    this.insultsUsed = insultsUsed;
  }

  public List<String> getComplimentsUsed() {
    if(complimentsUsed == null){
      complimentsUsed = new ArrayList<String>();
    }
    return complimentsUsed;
  }

  public void setComplimentsUsed(List<String> complimentsUsed) {
    this.complimentsUsed = complimentsUsed;
  }

  public List<String> getWinsultsUsed() {
    if(winsultsUsed == null){
      winsultsUsed = new ArrayList<String>();
    }
    return winsultsUsed;
  }

  public void setWinsultsUsed(List<String> winsultsUsed) {
    this.winsultsUsed = winsultsUsed;
  }

  public List<String> getBackhandedComplimentsUsed() {
    if(backhandedComplimentsUsed == null){
      backhandedComplimentsUsed = new ArrayList<String>();
    }
    return backhandedComplimentsUsed;
  }

  public void setBackhandedComplimentsUsed(List<String> backhandedComplimentsUsed) {
    this.backhandedComplimentsUsed = backhandedComplimentsUsed;
  }
}

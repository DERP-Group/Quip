package com.derpgroup.quip.model;

import java.util.List;

public class Quip {

  private String text;
  private String ssml;
  private String quipGroup;
  private List<QuipUsageRule> usageRules;
  
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public String getSsml() {
    return ssml;
  }
  public void setSsml(String ssml) {
    this.ssml = ssml;
  }
  public String getQuipGroup() {
    return quipGroup;
  }
  public void setQuipGroup(String quipGroup) {
    this.quipGroup = quipGroup;
  }
  public List<QuipUsageRule> getUsageRules() {
    return usageRules;
  }
  public void setUsageRules(List<QuipUsageRule> usageRules) {
    this.usageRules = usageRules;
  }
}

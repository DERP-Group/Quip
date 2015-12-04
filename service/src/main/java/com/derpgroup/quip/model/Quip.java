package com.derpgroup.quip.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Quip {

  private String text;
  private String ssml;
  private String quipGroup;
  private List<QuipUsageRule> usageRules;
  private List<String> tags;
  private String targetableText;
  private String targetableSsml;
  
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
  public List<String> getTags() {
    return tags;
  }
  public void setTags(List<String> tags) {
    this.tags = tags;
  }
  public String getTargetableText() {
    return targetableText;
  }
  public void setTargetableText(String targetableText) {
    this.targetableText = targetableText;
  }
  public String getTargetableSsml() {
    return targetableSsml;
  }
  public void setTargetableSsml(String targetableSsml) {
    this.targetableSsml = targetableSsml;
  }
  public boolean isTargetable(){
    return StringUtils.isNotEmpty(targetableText) && StringUtils.isNotEmpty(targetableSsml);
  }
  public String toString(){
    return "("+quipGroup+") "+text;
  }
}

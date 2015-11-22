package com.derpgroup.quip.model;

public class QuipUsageRule {
  private String userField;
  private String ruleType;  // TODO: Probably an enum
  private String value;
  
  public String getUserField() {
    return userField;
  }
  public void setUserField(String userField) {
    this.userField = userField;
  }
  public String getRuleType() {
    return ruleType;
  }
  public void setRuleType(String ruleType) {
    this.ruleType = ruleType;
  }
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
}

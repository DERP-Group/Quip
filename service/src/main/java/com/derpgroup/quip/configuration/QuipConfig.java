package com.derpgroup.quip.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuipConfig {
  
  @NotNull
  private String complimentsFile;
  
  @NotNull
  private String winsultsFile;
  
  @NotNull
  private String insultsFile;
  
  @NotNull
  private String backhandedComplimentsFile;
  
  @NotNull
  private int refreshRate;    // seconds
  
  @JsonProperty
  public String getComplimentsFile() {return complimentsFile;}
  
  @JsonProperty
  public void setComplimentsFile(String complimentsFile) {this.complimentsFile = complimentsFile;}

  @JsonProperty
  public String getWinsultsFile() {return winsultsFile;}

  @JsonProperty
  public void setWinsultsFile(String winsultsFile) {this.winsultsFile = winsultsFile;}

  @JsonProperty
  public String getInsultsFile() {return insultsFile;}
  
  @JsonProperty
  public void setInsultsFile(String insultsFile) {this.insultsFile = insultsFile;}

  @JsonProperty
  public String getBackhandedComplimentsFile() {return backhandedComplimentsFile;}

  @JsonProperty
  public void setBackhandedComplimentsFile(String backhandedComplimentsFile) {this.backhandedComplimentsFile = backhandedComplimentsFile;}
  
  @JsonProperty
  public int getRefreshRate() {return refreshRate;}
  
  @JsonProperty
  public void setRefreshRate(int refreshRate) {this.refreshRate = refreshRate;}
}

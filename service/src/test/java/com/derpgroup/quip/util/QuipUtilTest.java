package com.derpgroup.quip.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.quip.model.Quip;

public class QuipUtilTest {
  
  public Quip getBasicQuip(){
    Quip quip = new Quip();
    quip.setQuipGroup("I_LIGHT_UP");
    quip.setTags(null);
    quip.setUsageRules(null);
    quip.setText("I light up every time you talk to me!");
    quip.setSsml("I light up every time you talk to me!");
    quip.setTargetableText("I light up every time [TARGET] talks to me!");
    quip.setTargetableSsml("I light up every time [TARGET] talks to me!");
    return quip;
  }
  
  public Quip getAdvancedQuip(){
    Quip quip = new Quip();
    quip.setQuipGroup("STOCKHOLME");
    quip.setTags(null);
    quip.setUsageRules(null);
    quip.setText("Stockholme Syndrome is the only reason CompliBot likes you.");
    quip.setSsml("Stockholme Syndrome is the only reason [COMPLIBOT] likes you.");
    quip.setTargetableText("Stockholme Syndrome is the only reason CompliBot likes [TARGET].");
    quip.setTargetableSsml("Stockholme Syndrome is the only reason [COMPLIBOT] likes [TARGET].");
    return quip;
  }

  @Test
  public void testContentSubstitution_noValues() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    String response = QuipUtil.substituteContent("Is that really your name?", replacementValues);
    assertEquals("Is that really your name?",response);
  }
  
  @Test
  public void testContentSubstitution_null() throws DerpwizardException{
    String response = QuipUtil.substituteContent("Is that really your name?", null);
    assertEquals("Is that really your name?",response);
  }
  
  @Test
  public void testContentSubstitution_singleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[COMPLIBOT]", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>");
    String response = QuipUtil.substituteContent("How do you say [COMPLIBOT]?", replacementValues);
    assertEquals("How do you say <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>?",response);
  }
  
  @Test
  public void testContentSubstitution_multipleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[COMPLIBOT]", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>");
    replacementValues.put("[INSULTIBOT]", "InsultaBot");
    String response = QuipUtil.substituteContent("What does [COMPLIBOT] think about [INSULTIBOT]?", replacementValues);
    assertEquals("What does <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme> think about InsultaBot?",response);
  }
  
  @Test
  public void testContentSubstitution_multipleSubstitutionsWithOneValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[COMPLIBOT]", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>");
    String response = QuipUtil.substituteContent("[COMPLIBOT] loves [COMPLIBOT]", replacementValues);
    assertEquals("<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme> loves <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>",response);
  }

  @Test
  public void testContentSubstitution_frontEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[NAME]", "CompliBot");

    String response = QuipUtil.substituteContent("[NAME] is awesome!", replacementValues);
    assertEquals("CompliBot is awesome!",response);
  }
  
  @Test
  public void testContentSubstitution_backEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[NAME]", "CompliBot");

    String response = QuipUtil.substituteContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }
  
  @Test
  public void testContentSubstitution_unusedReplacementValues() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[NAME]", "CompliBot");
    replacementValues.put("[TITLE]", "the Great");
    replacementValues.put("[TARGET]", "InsultiBot");

    String response = QuipUtil.substituteContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }
  
  @Test
  public void testQuipSubstitution_bothNull(){
    Quip quip = getBasicQuip();
    Quip subbedQuip = QuipUtil.substituteContent(quip, null, null);
    assertEquals("I_LIGHT_UP",subbedQuip.getQuipGroup());
    assertEquals(null,subbedQuip.getTags());
    assertEquals(null,subbedQuip.getUsageRules());
    assertEquals("I light up every time you talk to me!",subbedQuip.getText());
    assertEquals("I light up every time you talk to me!",subbedQuip.getSsml());
    assertEquals("I light up every time [TARGET] talks to me!",subbedQuip.getTargetableText());
    assertEquals("I light up every time [TARGET] talks to me!",subbedQuip.getTargetableSsml());
  }
  
  @Test
  public void testQuipSubstitution_phoneticsNull(){
    Quip quip = getBasicQuip();
    
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[NAME]", "User 1");
    replacementValues.put("[GENDER_PRONOUN]", "his");
    replacementValues.put("[TARGET]", "User 2");
    
    Quip subbedQuip = QuipUtil.substituteContent(quip, null, replacementValues);
    assertEquals("I_LIGHT_UP",subbedQuip.getQuipGroup());
    assertEquals(null,subbedQuip.getTags());
    assertEquals(null,subbedQuip.getUsageRules());
    assertEquals("I light up every time you talk to me!",subbedQuip.getText());
    assertEquals("I light up every time you talk to me!",subbedQuip.getSsml());
    assertEquals("I light up every time User 2 talks to me!",subbedQuip.getTargetableText());
    assertEquals("I light up every time User 2 talks to me!",subbedQuip.getTargetableSsml());
  }
  
  @Test
  public void testQuipSubstitution_normalReplacementsNull(){
    Quip quip = getBasicQuip();
    
    Map<String, String> phoneticReplacements = new HashMap<String, String>();
    phoneticReplacements.put("[COMPLIBOT]", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>");
    phoneticReplacements.put("[INSULTIBOT]", "insultabot");
    
    Quip subbedQuip = QuipUtil.substituteContent(quip, phoneticReplacements, null);
    assertEquals("I_LIGHT_UP",subbedQuip.getQuipGroup());
    assertEquals(null,subbedQuip.getTags());
    assertEquals(null,subbedQuip.getUsageRules());
    assertEquals("I light up every time you talk to me!",subbedQuip.getText());
    assertEquals("I light up every time you talk to me!",subbedQuip.getSsml());
    assertEquals("I light up every time [TARGET] talks to me!",subbedQuip.getTargetableText());
    assertEquals("I light up every time [TARGET] talks to me!",subbedQuip.getTargetableSsml());
  }
  
  @Test
  public void testQuipSubstitution_normalReplacements(){
    Quip quip = getAdvancedQuip();
    
    Map<String, String> phoneticReplacements = new HashMap<String, String>();
    phoneticReplacements.put("[COMPLIBOT]", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme>");
    phoneticReplacements.put("[INSULTIBOT]", "insultabot");
    
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("[GENDER_POSSESSIVE_PRONOUN]", "his");
    replacementValues.put("[GENDER_OBJECT_PRONOUN]", "him");
    replacementValues.put("[GENDER_SUBJECT_PRONOUN]", "he");
    replacementValues.put("[TARGET]", "TargetName");
    
    Quip subbedQuip = QuipUtil.substituteContent(quip, phoneticReplacements, replacementValues);
    assertEquals("STOCKHOLME",subbedQuip.getQuipGroup());
    assertEquals(null,subbedQuip.getTags());
    assertEquals(null,subbedQuip.getUsageRules());
    assertEquals("Stockholme Syndrome is the only reason CompliBot likes you.",subbedQuip.getText());
    assertEquals("Stockholme Syndrome is the only reason <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme> likes you.",subbedQuip.getSsml());
    assertEquals("Stockholme Syndrome is the only reason CompliBot likes TargetName.",subbedQuip.getTargetableText());
    assertEquals("Stockholme Syndrome is the only reason <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> CompliBot </phoneme> likes TargetName.",subbedQuip.getTargetableSsml());
  }

}

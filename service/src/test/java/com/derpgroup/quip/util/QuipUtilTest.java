package com.derpgroup.quip.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;

public class QuipUtilTest {

  @Test
  public void testContentSubstitution_noValues() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    String response = QuipUtil.substituteContent("Is that really your name?", replacementValues);
    assertEquals("Is that really your name?",response);
  }
  
  @Test
  public void testContentSubstitution_singleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("CompliBot", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme>");
    String response = QuipUtil.substituteContent("How do you say CompliBot?", replacementValues);
    assertEquals("How do you say <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme>?",response);
  }
  
  @Test
  public void testContentSubstitution_multipleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("CompliBot", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme>");
    replacementValues.put("InsultiBot", "InsultaBot");
    String response = QuipUtil.substituteContent("What does CompliBot think about InsultiBot?", replacementValues);
    assertEquals("What does <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme> think about InsultaBot?",response);
  }
  
  @Test
  public void testContentSubstitution_multipleSubstitutionsWithOneValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("CompliBot", "<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme>");
    String response = QuipUtil.substituteContent("CompliBot loves CompliBot", replacementValues);
    assertEquals("<phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme> loves <phoneme alphabet=\"ipa\" ph=\"kɒmpləbɑt\"> complibot </phoneme>",response);
  }
  
  @Test
  public void testBracketedContentSubstitution_singleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    String response = QuipUtil.substituteBracketedContent("Is your name really [NAME]?", replacementValues);
    assertEquals("Is your name really CompliBot?",response);
  }
  
  @Test
  public void testBracketedContentSubstitution_multipleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    replacementValues.put("TARGET", "InsultiBot");
    String response = QuipUtil.substituteBracketedContent("[NAME] [TITLE]'s love for [TARGET] was unconditional.", replacementValues);
    assertEquals("CompliBot the Great's love for InsultiBot was unconditional.",response);
  }
  
  @Test
  public void testBracketedContentSubstitution_multipleSubstitutionsWithOneValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    String response = QuipUtil.substituteBracketedContent("[NAME]'s love for [NAME] was unconditional... just as [NAME] would have wanted it.", replacementValues);
    assertEquals("CompliBot's love for CompliBot was unconditional... just as CompliBot would have wanted it.",response);
  }

  @Test
  public void testBracketedContentSubstitution_frontEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");

    String response = QuipUtil.substituteBracketedContent("[NAME] is awesome!", replacementValues);
    assertEquals("CompliBot is awesome!",response);
  }
  
  @Test
  public void testBracketedContentSubstitution_backEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");

    String response = QuipUtil.substituteBracketedContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }
  
  @Test
  public void testBracketedContentSubstitution_unusedReplacementValues() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    replacementValues.put("TARGET", "InsultiBot");

    String response = QuipUtil.substituteBracketedContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }

}

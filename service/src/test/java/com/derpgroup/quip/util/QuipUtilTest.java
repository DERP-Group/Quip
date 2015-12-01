package com.derpgroup.quip.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    response = QuipUtil.substituteContent("Is that really your NAME?", replacementValues);
    assertEquals("Is that really your NAME?",response);
  }
  
  @Test
  public void testContentSubstitution_singleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    String response = QuipUtil.substituteContent("Is your name really [NAME]?", replacementValues);
    assertEquals("Is your name really CompliBot?",response);
  }
  
  @Test
  public void testContentSubstitution_multipleValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    replacementValues.put("TARGET", "InsultiBot");
    String response = QuipUtil.substituteContent("[NAME] [TITLE]'s love for [TARGET] was unconditional.", replacementValues);
    assertEquals("CompliBot the Great's love for InsultiBot was unconditional.",response);
  }
  
  @Test
  public void testContentSubstitution_multipleSubstitutionsWithOneValue() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    String response = QuipUtil.substituteContent("[NAME]'s love for [NAME] was unconditional... just as [NAME] would have wanted it.", replacementValues);
    assertEquals("CompliBot's love for CompliBot was unconditional... just as CompliBot would have wanted it.",response);
  }
  
  @Test
  public void testContentSubstitution_emptyBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    try{
      QuipUtil.substituteContent("Is your name really []?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Unknown substitution value of"));
    }
  }
  
  @Test
  public void testContentSubstitution_substituteNotFound() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("TITLE", "the Great");
    replacementValues.put("TARGET", "InsultiBot");
    try{
      QuipUtil.substituteContent("Is your name really [NAME]?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Unknown substitution value of"));
    }
  }

  @Test
  public void testContentSubstitution_frontEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");

    String response = QuipUtil.substituteContent("[NAME] is awesome!", replacementValues);
    assertEquals("CompliBot is awesome!",response);
  }
  
  @Test
  public void testContentSubstitution_backEdgeBrackets() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");

    String response = QuipUtil.substituteContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }
  
  @Test
  public void testContentSubstitution_unusedReplacementValues() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    replacementValues.put("TARGET", "InsultiBot");

    String response = QuipUtil.substituteContent("Who's awesome? [NAME]", replacementValues);
    assertEquals("Who's awesome? CompliBot",response);
  }
  
  @Test
  public void testContentSubstitution_unmatchedOpenBracket_complex() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    try{
      QuipUtil.substituteContent("Is your name really [NAME] [TITLE?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Missing closing bracket detected in content"));
    }
  }
  
  @Test
  public void testContentSubstitution_unmatchedCloseBracket_complex() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    replacementValues.put("TITLE", "the Great");
    try{
      QuipUtil.substituteContent("Is your name really [NAME] TITLE]?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Missing opening bracket detected in content"));
    }
  }
  
  @Test
  public void testContentSubstitution_unmatchedCloseBracket() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    try{
      QuipUtil.substituteContent("Is your name really NAME]?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Missing opening bracket detected in content"));
    }
  }
  
  @Test
  public void testContentSubstitution_unmatchedOpenBracket() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    try{
      QuipUtil.substituteContent("Is your name really [NAME?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Missing closing bracket detected in content"));
    }
  }
  
  @Test
  public void testContentSubstitution_doubleBracket() throws DerpwizardException{
    Map<String, String> replacementValues = new HashMap<String, String>();
    replacementValues.put("NAME", "CompliBot");
    try{
      QuipUtil.substituteContent("Is your name really [[NAME]]?", replacementValues);
    }
    catch(DerpwizardException e){
      assertTrue(e.getMessage().startsWith("Unknown substitution value of"));
    }
  }
}

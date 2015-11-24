package com.derpgroup.quip.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.AlexaInput;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.SsmlDocument;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.quip.QuipMetadata;
import com.derpgroup.quip.manager.QuipManager.BackhandedCompliments;
import com.derpgroup.quip.manager.QuipManager.Compliments;
import com.derpgroup.quip.manager.QuipManager.Insults;
import com.derpgroup.quip.manager.QuipManager.Winsults;

public class QuipManagerTest {

  QuipManager manager;
  QuipMetadata metadata;
  SsmlDocumentBuilder builder;
  
  @Before
  public void setup(){
    manager = new QuipManager();
    builder = new SsmlDocumentBuilder();
    metadata = new QuipMetadata();
    metadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
  }
  
  @Test
  public void testConversationRequest_another_noConversationHistory() throws DerpwizardException{
    
    metadata.setBot("complibot");
    IntentRequest intentRequest = IntentRequest.builder().withRequestId("123").withIntent(Intent.builder().withName("ANOTHER").build()).build();
    AlexaInput alexaInput = new AlexaInput(intentRequest, metadata);
    
    manager.doConversationRequest(alexaInput, builder);
    
    SsmlDocument output = builder.build();
    assertNotNull(output);
    String outputString = output.getSsml();
    assertNotNull(outputString);
    
    String outputRawText = builder.getRawText();
    assertNotNull(outputRawText);
    assertTrue(outputRawText.length() > 0);
    assertThat(outputRawText, not(equalTo("I don't know how to handle requests for unnamed bots.")));
  }
  
  @Test
  public void testConversationRequest_another_noConversationHistory_noBot() throws DerpwizardException{
    
    IntentRequest intentRequest = IntentRequest.builder().withRequestId("123").withIntent(Intent.builder().withName("ANOTHER").build()).build();
    AlexaInput alexaInput = new AlexaInput(intentRequest, metadata);
    
    manager.doConversationRequest(alexaInput, builder);
    
    SsmlDocument output = builder.build();
    assertNotNull(output);
    String outputString = output.getSsml();
    assertNotNull(outputString);
    
    String outputRawText = builder.getRawText();
    assertNotNull(outputRawText);
    assertTrue(outputRawText.length() > 0);
    assertEquals(outputRawText,"I don't know how to handle requests for unnamed bots.");
  }
  
  @Test
  public void testDetermineMaxQuipHistorySize(){
    assertEquals(QuipManager.determineMaxQuipHistorySize(2*QuipManager.MAXIMUM_QUIP_HISTORY_SIZE+10),QuipManager.MAXIMUM_QUIP_HISTORY_SIZE);
    assertEquals(QuipManager.determineMaxQuipHistorySize(2*QuipManager.MAXIMUM_QUIP_HISTORY_SIZE),QuipManager.MAXIMUM_QUIP_HISTORY_SIZE);
    assertEquals(QuipManager.determineMaxQuipHistorySize(2*QuipManager.MAXIMUM_QUIP_HISTORY_SIZE-1),QuipManager.MAXIMUM_QUIP_HISTORY_SIZE-1);
    assertEquals(QuipManager.determineMaxQuipHistorySize(QuipManager.MAXIMUM_QUIP_HISTORY_SIZE),(int)(QuipManager.MAXIMUM_QUIP_HISTORY_SIZE*QuipManager.MAXIMUM_QUIP_HISTORY_PERCENT));
    assertEquals(QuipManager.determineMaxQuipHistorySize(0),0);
  }
  
  @Test
  public void testCompliments_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> originalComplimentUsed = new ArrayDeque<String>();
    for(int i=0; i<Compliments.values().length/2; i++){
      originalComplimentUsed.add(Compliments.values()[i].name());
    }
    for(int i=0; i<5; i++){
      Deque<String> complimentsUsed = new ArrayDeque<String>();
      complimentsUsed.addAll(originalComplimentUsed);
      quipMetadata.setComplimentsUsed(complimentsUsed);
      Compliments compliment = manager.doComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalComplimentUsed.contains(compliment.name()));
    }
  }
  
  @Test
  public void testWinsults_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
   
    Deque<String> originalWinsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<Winsults.values().length/2; i++){
      originalWinsultsUsed.add(Winsults.values()[i].name());
    }
    for(int i=0; i<5; i++){
      Deque<String> winsultsUsed = new ArrayDeque<String>();
      winsultsUsed.addAll(originalWinsultsUsed);
      quipMetadata.setWinsultsUsed(winsultsUsed);
      Winsults winsult = manager.doWinsultRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalWinsultsUsed.contains(winsult.name()));
    }
  }
  
  @Test
  public void testInsults_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> originalInsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<Insults.values().length/2; i++){
      originalInsultsUsed.add(Insults.values()[i].name());
    }
    for(int i=0; i<5; i++){
      Deque<String> insultsUsed = new ArrayDeque<String>();
      insultsUsed.addAll(originalInsultsUsed);
      quipMetadata.setInsultsUsed(insultsUsed);
      Insults insult = manager.doInsultRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalInsultsUsed.contains(insult.name()));
    }
  }
  
  @Test
  public void testBackhandedCompliments_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> originalBackhandedComplimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<BackhandedCompliments.values().length/2; i++){
      originalBackhandedComplimentsUsed.add(BackhandedCompliments.values()[i].name());
    }
    for(int i=0; i<5; i++){
      Deque<String> backhandedComplimentsUsed = new ArrayDeque<String>();
      backhandedComplimentsUsed.addAll(originalBackhandedComplimentsUsed);
      quipMetadata.setBackhandedComplimentsUsed(backhandedComplimentsUsed);
      BackhandedCompliments backhandedCompliment = manager.doBackhandedComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalBackhandedComplimentsUsed.contains(backhandedCompliment.name()));
    }
  }
  
  @Test
  public void testCompliments_registersInHistory(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> complimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<Compliments.values().length/2; i++){
      complimentsUsed.add(Compliments.values()[i].name());
    }

    quipMetadata.setComplimentsUsed(complimentsUsed);
    Compliments compliment = manager.doComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(complimentsUsed.getLast(),compliment.name());
  }
  
  @Test
  public void testWinsults_registersInHistory(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> winsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<Winsults.values().length/2; i++){
      winsultsUsed.add(Winsults.values()[i].name());
    }
    quipMetadata.setWinsultsUsed(winsultsUsed);
    Winsults winsult = manager.doWinsultRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(winsultsUsed.getLast(),winsult.name());
  }
  
  @Test
  public void testInsults_registersInHistory(){

    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> insultsUsed = new ArrayDeque<String>();
    for(int i=0; i<Insults.values().length/2; i++){
      insultsUsed.add(Insults.values()[i].name());
    }
    quipMetadata.setInsultsUsed(insultsUsed);
    Insults insult = manager.doInsultRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(insultsUsed.getLast(),insult.name());
  }
  
  @Test
  public void testBackhandedCompliments_registersInHistory(){

    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> backhandedComplimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<BackhandedCompliments.values().length/2; i++){
      backhandedComplimentsUsed.add(BackhandedCompliments.values()[i].name());
    }
    quipMetadata.setBackhandedComplimentsUsed(backhandedComplimentsUsed);
    BackhandedCompliments backhandedCompliment = manager.doBackhandedComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(backhandedComplimentsUsed.getLast(),backhandedCompliment.name());
  }
}

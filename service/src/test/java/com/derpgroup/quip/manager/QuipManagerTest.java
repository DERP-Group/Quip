package com.derpgroup.quip.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
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
import com.derpgroup.quip.configuration.QuipConfig;
import com.derpgroup.quip.model.Quip;
import com.derpgroup.quip.model.QuipStore;

public class QuipManagerTest {

  QuipManager manager;
  QuipMetadata metadata;
  SsmlDocumentBuilder builder;
  
  @BeforeClass
  public static void beforeClass_setup() throws IOException {
    QuipStore quipStore = QuipStore.getInstance();
    if(quipStore.isInitialized()){return;}
    
    QuipConfig config = new QuipConfig();
    config.setRefreshRate(10);
    File currentDir = new File(".");
    config.setComplimentsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/complibot/compliments.json");
    config.setWinsultsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/complibot/winsults.json");
    config.setInsultsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/insultibot/insults.json");
    config.setBackhandedComplimentsFile(currentDir.getCanonicalPath()+"/src/main/resources/quips/insultibot/backhandedCompliments.json");
    quipStore.init(config);
  }
  
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
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.COMPLIMENT);
    for(int i=0; i<quips.size()/2; i++){
      originalComplimentUsed.add(quips.get(i).getQuipGroup());
    }
    for(int i=0; i<5; i++){
      Deque<String> complimentsUsed = new ArrayDeque<String>();
      complimentsUsed.addAll(originalComplimentUsed);
      quipMetadata.setComplimentsUsed(complimentsUsed);
      Quip compliment = manager.doComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalComplimentUsed.contains(compliment.getQuipGroup()));
    }
  }
  
  @Test
  public void testWinsults_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.WINSULT);
   
    Deque<String> originalWinsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      originalWinsultsUsed.add(quips.get(i).getQuipGroup());
    }
    for(int i=0; i<5; i++){
      Deque<String> winsultsUsed = new ArrayDeque<String>();
      winsultsUsed.addAll(originalWinsultsUsed);
      quipMetadata.setWinsultsUsed(winsultsUsed);
      Quip winsult = manager.doWinsultRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalWinsultsUsed.contains(winsult.getQuipGroup()));
    }
  }
  
  @Test
  public void testInsults_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.INSULT);
    
    Deque<String> originalInsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      originalInsultsUsed.add(quips.get(i).getQuipGroup());
    }
    for(int i=0; i<5; i++){
      Deque<String> insultsUsed = new ArrayDeque<String>();
      insultsUsed.addAll(originalInsultsUsed);
      quipMetadata.setInsultsUsed(insultsUsed);
      Quip insult = manager.doInsultRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalInsultsUsed.contains(insult.getQuipGroup()));
    }
  }
  
  @Test
  public void testBackhandedCompliments_NoRepeats(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.BACKHANDED_COMPLIMENT);
    
    Deque<String> originalBackhandedComplimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      originalBackhandedComplimentsUsed.add(quips.get(i).getQuipGroup());
    }
    for(int i=0; i<5; i++){
      Deque<String> backhandedComplimentsUsed = new ArrayDeque<String>();
      backhandedComplimentsUsed.addAll(originalBackhandedComplimentsUsed);
      quipMetadata.setBackhandedComplimentsUsed(backhandedComplimentsUsed);
      Quip backhandedCompliment = manager.doBackhandedComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
      assertTrue(!originalBackhandedComplimentsUsed.contains(backhandedCompliment.getQuipGroup()));
    }
  }
  
  @Test
  public void testCompliments_registersInHistory(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.COMPLIMENT);
    
    Deque<String> complimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      complimentsUsed.add(quips.get(i).getQuipGroup());
    }

    quipMetadata.setComplimentsUsed(complimentsUsed);
    Quip compliment = manager.doComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(complimentsUsed.getLast(),compliment.getQuipGroup());
  }
  
  @Test
  public void testWinsults_registersInHistory(){
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.WINSULT);
    
    Deque<String> winsultsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      winsultsUsed.add(quips.get(i).getQuipGroup());
    }
    quipMetadata.setWinsultsUsed(winsultsUsed);
    Quip winsult = manager.doWinsultRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(winsultsUsed.getLast(),winsult.getQuipGroup());
  }
  
  @Test
  public void testInsults_registersInHistory(){

    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.INSULT);
    
    Deque<String> insultsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      insultsUsed.add(quips.get(i).getQuipGroup());
    }
    quipMetadata.setInsultsUsed(insultsUsed);
    Quip insult = manager.doInsultRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(insultsUsed.getLast(),insult.getQuipGroup());
  }
  
  @Test
  public void testBackhandedCompliments_registersInHistory(){

    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.BACKHANDED_COMPLIMENT);
    
    Deque<String> backhandedComplimentsUsed = new ArrayDeque<String>();
    for(int i=0; i<quips.size()/2; i++){
      backhandedComplimentsUsed.add(quips.get(i).getQuipGroup());
    }
    quipMetadata.setBackhandedComplimentsUsed(backhandedComplimentsUsed);
    Quip backhandedCompliment = manager.doBackhandedComplimentRequest(new HashMap<String,String>(), builder, quipMetadata);
    assertEquals(backhandedComplimentsUsed.getLast(),backhandedCompliment.getQuipGroup());
  }
  
  @Test(timeout=1000)
  public void testRandomTargetableQuip_NoInfiniteLoops() throws DerpwizardException{
    
    QuipMetadata quipMetadata = new QuipMetadata();
    quipMetadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
    Deque<String> originalComplimentUsed = new ArrayDeque<String>();
    List<Quip> quips = QuipStore.getInstance().getQuips(QuipType.COMPLIMENT);
    for(int i=0; i<quips.size(); i++){
      originalComplimentUsed.add(quips.get(i).getQuipGroup());
    }
    
    manager.getRandomTargetableQuip(QuipType.COMPLIMENT, originalComplimentUsed, "that guy");
  }
}

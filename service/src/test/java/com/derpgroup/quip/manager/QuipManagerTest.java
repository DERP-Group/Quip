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

import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.ServiceInput;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
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
    manager = new QuipManager(null);
    builder = new SsmlDocumentBuilder();
    metadata = new QuipMetadata();
    metadata.setConversationHistory(new ArrayDeque<ConversationHistoryEntry>());
  }
  
  @Test
  public void testConversationRequest_another_noConversationHistory() throws DerpwizardException{
    
    metadata.setBot("complibot");
    
    ServiceInput serviceInput = new ServiceInput();
    serviceInput.setMetadata(metadata);
    serviceInput.setSubject("ANOTHER");
    serviceInput.setMessageAsMap(new HashMap<String,String>());
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(metadata);
    serviceOutput.setConversationEnded(false);
    
    manager.handleRequest(serviceInput, serviceOutput);
    
    assertNotNull(serviceOutput.getVoiceOutput());
    String outputString = serviceOutput.getVoiceOutput().getSsmltext();
    assertNotNull(outputString);
    
    String outputRawText = serviceOutput.getVisualOutput().getText();
    assertNotNull(outputRawText);
    assertTrue(outputRawText.length() > 0);
    assertThat(outputRawText, not(equalTo("I don't know how to handle requests for unnamed bots.")));
  }
  
  @Test
  public void testConversationRequest_another_noConversationHistory_noBot() throws DerpwizardException{
    
    ServiceInput serviceInput = new ServiceInput();
    serviceInput.setMetadata(metadata);
    serviceInput.setSubject("ANOTHER");
    serviceInput.setMessageAsMap(new HashMap<String,String>());
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(metadata);
    serviceOutput.setConversationEnded(false);
    manager.handleRequest(serviceInput, serviceOutput);
    
    assertNotNull(serviceOutput.getVoiceOutput());
    String outputString = serviceOutput.getVoiceOutput().getSsmltext();
    assertNotNull(outputString);
    
    String outputRawText = serviceOutput.getVisualOutput().getText();
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
      
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(quipMetadata);
      serviceOutput.setConversationEnded(false);
      
      Quip compliment = manager.doComplimentRequest(null, serviceOutput);
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
      
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(quipMetadata);
      serviceOutput.setConversationEnded(false);
      
      Quip winsult = manager.doWinsultRequest(null, serviceOutput);
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
      
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(quipMetadata);
      serviceOutput.setConversationEnded(false);
      
      Quip insult = manager.doInsultRequest(null, serviceOutput);
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
      
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(quipMetadata);
      serviceOutput.setConversationEnded(false);
      
      Quip backhandedCompliment = manager.doBackhandedComplimentRequest(null, serviceOutput);
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
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(quipMetadata);
    serviceOutput.setConversationEnded(false);
    
    Quip compliment = manager.doComplimentRequest(null, serviceOutput);
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
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(quipMetadata);
    serviceOutput.setConversationEnded(false);
    
    Quip winsult = manager.doWinsultRequest(null, serviceOutput);
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
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(quipMetadata);
    serviceOutput.setConversationEnded(false);
    
    Quip insult = manager.doInsultRequest(null, serviceOutput);
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
    
    ServiceOutput serviceOutput = new ServiceOutput();
    serviceOutput.setMetadata(quipMetadata);
    serviceOutput.setConversationEnded(false);
    
    Quip backhandedCompliment = manager.doBackhandedComplimentRequest(null, serviceOutput);
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

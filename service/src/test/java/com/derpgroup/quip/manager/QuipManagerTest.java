package com.derpgroup.quip.manager;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.derpgroup.derpwizard.voice.model.AlexaInput;
import com.derpgroup.derpwizard.voice.model.ConversationHistoryEntry;
import com.derpgroup.derpwizard.voice.model.SsmlDocument;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.quip.QuipMetadata;

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
  public void testConversationRequest_another_noConversationHistory(){
    
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
  public void testConversationRequest_another_noConversationHistory_noBot(){
    
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
}

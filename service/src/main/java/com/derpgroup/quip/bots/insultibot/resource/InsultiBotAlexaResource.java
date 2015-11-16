/**
 * Copyright (C) 2015 David Phillips
 * Copyright (C) 2015 Eric Olson
 * Copyright (C) 2015 Rusty Gerard
 * Copyright (C) 2015 Paul Winters
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.derpgroup.quip.bots.insultibot.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.derpgroup.derpwizard.voice.model.SsmlDocumentBuilder;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.model.VoiceMessageFactory;
import com.derpgroup.derpwizard.voice.model.VoiceOutput;
import com.derpgroup.derpwizard.voice.model.VoiceInput.MessageType;
import com.derpgroup.derpwizard.voice.model.VoiceMessageFactory.InterfaceType;
import com.derpgroup.quip.MixInModule;
import com.derpgroup.quip.QuipMetadata;
import com.derpgroup.quip.configuration.MainConfig;
import com.derpgroup.quip.manager.QuipManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST APIs for requests generating from Amazon Alexa
 *
 * @author Eric
 * @since 0.0.1
 */
@Path("/insultibot/alexa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InsultiBotAlexaResource {

  private static final List<String> UNSUPPORTED_SSML_TAGS = Collections.unmodifiableList(Arrays.asList(
      "emphasis"
      ));

  private QuipManager manager;
  
  public InsultiBotAlexaResource(MainConfig config, Environment env) {
    manager = new QuipManager();
  }

  /**
   * Generates a welcome message.
   *
   * @return The message, never null
   * @throws IOException 
   */
  @POST
  public SpeechletResponseEnvelope doAlexaRequest(SpeechletRequestEnvelope request) throws IOException{
    if (request.getRequest() == null) {
      throw new RuntimeException("Missing request body."); //TODO: create AlexaException
    }

    Map<String, Object> sessionAttributes = request.getSession().getAttributes();
    sessionAttributes.put("bot", "insultibot");

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new MixInModule());
    QuipMetadata metadata = mapper.convertValue(sessionAttributes, new TypeReference<QuipMetadata>(){});

    SsmlDocumentBuilder builder = new SsmlDocumentBuilder(UNSUPPORTED_SSML_TAGS);
    VoiceInput voiceInput = VoiceMessageFactory.buildInputMessage(request.getRequest(), metadata, InterfaceType.ALEXA);
    builder.conversationEnd(false);
    manager.handleRequest(voiceInput, builder);

    Map<String,Object> sessionAttributesOutput = mapper.convertValue(metadata, new TypeReference<Map<String,Object>>(){});
    SpeechletResponseEnvelope responseEnvelope = new SpeechletResponseEnvelope();
    responseEnvelope.setSessionAttributes(sessionAttributesOutput);
    
    SpeechletResponse speechletResponse = new SpeechletResponse();
    
    SimpleCard card = new SimpleCard();
    card.setContent(builder.getRawText());
    card.setTitle("Alexa + Insultibot");
    
    SsmlOutputSpeech outputSpeech;
    // Create a VoiceOutput object with the SSML content generated by the manager
    if ((voiceInput.getMessageType() != MessageType.END_OF_CONVERSATION) && (voiceInput.getMessageType() != MessageType.STOP) && (voiceInput.getMessageType() != MessageType.CANCEL)) {

      @SuppressWarnings("unchecked")
      VoiceOutput<SsmlOutputSpeech> voiceOutput = (VoiceOutput<SsmlOutputSpeech>) VoiceMessageFactory.buildOutputMessage(builder.build(), InterfaceType.ALEXA);
      outputSpeech = voiceOutput.getImplInstance();
    }else{
      outputSpeech = new SsmlOutputSpeech();
      outputSpeech.setSsml("<speak></speak>");
      card.setContent("Goodbye!");
      speechletResponse.setShouldEndSession(true);
    }
    speechletResponse.setShouldEndSession(builder.isConversationEnd());
    speechletResponse.setOutputSpeech(outputSpeech);
    speechletResponse.setCard(card);

    responseEnvelope.setResponse(speechletResponse);

    return responseEnvelope;
  }
}

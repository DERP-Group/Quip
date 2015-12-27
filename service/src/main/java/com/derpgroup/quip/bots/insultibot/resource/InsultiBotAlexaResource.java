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

import java.util.Map;

import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException;
import com.derpgroup.derpwizard.voice.exception.DerpwizardExceptionAlexaWrapper;
import com.derpgroup.derpwizard.voice.exception.DerpwizardException.DerpwizardExceptionReasons;
import com.derpgroup.derpwizard.alexa.AlexaUtils;
import com.derpgroup.derpwizard.voice.model.CommonMetadata;
import com.derpgroup.derpwizard.voice.model.ServiceOutput;
import com.derpgroup.derpwizard.voice.model.VoiceInput;
import com.derpgroup.derpwizard.voice.model.VoiceMessageFactory;
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
  
  private static final Logger LOG = LoggerFactory.getLogger(InsultiBotAlexaResource.class);

  private QuipManager manager;
  
  public InsultiBotAlexaResource(MainConfig config, Environment env) {
    manager = new QuipManager(config);
  }

  /**
   * Generates a welcome message.
   *
   * @return The message, never null
   */
  @POST
  public SpeechletResponseEnvelope doAlexaRequest(SpeechletRequestEnvelope request, @HeaderParam("SignatureCertChainUrl") String signatureCertChainUrl, 
      @HeaderParam("Signature") String signature, @QueryParam("testFlag") Boolean testFlag){
    
    ObjectMapper mapper = new ObjectMapper();
    CommonMetadata outputMetadata = null;
    try {
      if (request.getRequest() == null) {
        throw new DerpwizardException(DerpwizardExceptionReasons.MISSING_INFO.getSsml(),"Missing request body.");
      }
      if(testFlag == null || testFlag == false){ 
        AlexaUtils.validateAlexaRequest(request, signatureCertChainUrl, signature);
      }
  
      Map<String, Object> sessionAttributes = request.getSession().getAttributes();
      sessionAttributes.put("bot", "insultibot");
  
      mapper.registerModule(new MixInModule());
      CommonMetadata inputMetadata = mapper.convertValue(sessionAttributes, new TypeReference<QuipMetadata>(){});
      outputMetadata = mapper.convertValue(sessionAttributes, new TypeReference<QuipMetadata>(){});
      
      // Build the ServiceOutput object, which gets updated within the service itself
      ServiceOutput serviceOutput = new ServiceOutput();
      serviceOutput.setMetadata(outputMetadata);
      serviceOutput.setConversationEnded(false);
  
      // Perform the service request
      VoiceInput voiceInput = VoiceMessageFactory.buildInputMessage(request.getRequest(), inputMetadata, InterfaceType.ALEXA);
      manager.handleRequest(voiceInput, serviceOutput);

      // Build the response
      SpeechletResponseEnvelope responseEnvelope = new SpeechletResponseEnvelope();
      Map<String,Object> sessionAttributesOutput = mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){});
      responseEnvelope.setSessionAttributes(sessionAttributesOutput);

      SpeechletResponse speechletResponse = new SpeechletResponse();
      SimpleCard card;
      Reprompt reprompt;
      SsmlOutputSpeech outputSpeech;
      
      switch(voiceInput.getMessageType()){
      case END_OF_CONVERSATION:
      case STOP:
      case CANCEL:
        outputSpeech = null;
        card = null;
        reprompt = null;
        speechletResponse.setShouldEndSession(true);
        break;
      default:
        if(StringUtils.isNotEmpty(serviceOutput.getVisualOutput().getTitle())&&
            StringUtils.isNotEmpty(serviceOutput.getVisualOutput().getText())){
          card = new SimpleCard();
          card.setTitle(serviceOutput.getVisualOutput().getTitle());
          card.setContent(serviceOutput.getVisualOutput().getText());
        }
        else{
          card = null;
        }
        if(serviceOutput.getDelayedVoiceOutput() !=null && StringUtils.isNotEmpty(serviceOutput.getDelayedVoiceOutput().getSsmltext())){
          reprompt = new Reprompt();
          SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
          repromptSpeech.setSsml("<speak>"+serviceOutput.getDelayedVoiceOutput().getSsmltext()+"</speak>");
          reprompt.setOutputSpeech(repromptSpeech);
        }
        else{
          reprompt = null;
        }
        
        outputSpeech = new SsmlOutputSpeech();
        outputSpeech.setSsml("<speak>"+serviceOutput.getVoiceOutput().getSsmltext()+"</speak>");
        speechletResponse.setShouldEndSession(serviceOutput.isConversationEnded());
        break;
      }

      speechletResponse.setOutputSpeech(outputSpeech);
      speechletResponse.setCard(card);
      speechletResponse.setReprompt(reprompt);
      responseEnvelope.setResponse(speechletResponse);

      return responseEnvelope;
    }
    catch(DerpwizardException e){
      LOG.debug(e.getMessage());
      return new DerpwizardExceptionAlexaWrapper(e, "1.0",mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){}));
    }
    catch(Throwable t){
      LOG.debug(t.getMessage());
      return new DerpwizardExceptionAlexaWrapper(new DerpwizardException(t.getMessage()),"1.0", mapper.convertValue(outputMetadata, new TypeReference<Map<String,Object>>(){}));
    }
  }
}

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

package com.derpgroup.derpwizard.resource;

import io.dropwizard.setup.Environment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.derpgroup.derpwizard.configuration.MainConfig;
import com.derpgroup.derpwizard.voice.alexa.AlexaRequestType;
import com.derpgroup.derpwizard.voice.alexa.AlexaSkillsKitUtil;

/**
 * REST APIs for requests generating from Amazon Alexa
 *
 * @author Eric
 * @since 0.0.1
 */
@Path("/alexa")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlexaResource {

  public AlexaResource(MainConfig config, Environment env) {}

  /**
   * Generates a welcome message.
   *
   * @return The message, never null
   */
  @POST
  public SpeechletResponseEnvelope doAlexaRequest(SpeechletRequestEnvelope request){
    if(request == null || request.getRequest() == null){
      throw new RuntimeException("Missing request body."); //TODO: create AlexaException
    }
    SpeechletRequest sr = request.getRequest();
    AlexaRequestType requestType = AlexaSkillsKitUtil.getRequestType(sr);
    
    switch(requestType){
    case LAUNCH_REQUEST:
      return doLaunchRequest(request);
    case INTENT_REQUEST:
      return doIntentRequest(request);
    case SESSION_ENDED_REQUEST:
      return doSessionEndedRequest(request);
      default: 
        throw new RuntimeException("Unknown request type."); //TODO: create AlexaException
    }
  }
  
  protected SpeechletResponseEnvelope doLaunchRequest(SpeechletRequestEnvelope request){
    SpeechletResponseEnvelope response = new SpeechletResponseEnvelope();
    response.setVersion(AlexaResource.class.getPackage().getImplementationVersion());
    
    SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
    outputSpeech.setSsml("<speak><p><s>Hi. This is Alexa, speaking on behalf of DerpWizard.</s></p></speak>");
    
    SimpleCard card = new SimpleCard();
    card.setContent("Hi. This is Alexa, speaking on behalf of DerpWizard.");
    card.setTitle("Alexa + DERPWizard");

    SpeechletResponse sr = new SpeechletResponse();
    sr.setOutputSpeech(outputSpeech);
    sr.setCard(card);
    response.setResponse(sr);
    response.setSessionAttributes(request.getSession().getAttributes());
    
    return response;
  }
  
  protected SpeechletResponseEnvelope doIntentRequest(SpeechletRequestEnvelope request){
    SpeechletResponseEnvelope response = new SpeechletResponseEnvelope();
    response.setVersion(AlexaResource.class.getPackage().getImplementationVersion());
    
    SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
    outputSpeech.setSsml("<speak><p><s>Hi. This is Alexa, speaking on behalf of DerpWizard.</s></p></speak>");
    
    SimpleCard card = new SimpleCard();
    card.setContent("Hi. This is Alexa, speaking on behalf of DerpWizard.");
    card.setTitle("Alexa + DERPWizard");

    SpeechletResponse sr = new SpeechletResponse();
    sr.setOutputSpeech(outputSpeech);
    sr.setCard(card);
    response.setResponse(sr);
    response.setSessionAttributes(request.getSession().getAttributes());
    return response;
  }
  
  protected SpeechletResponseEnvelope doSessionEndedRequest(SpeechletRequestEnvelope request){
    SpeechletResponseEnvelope response = new SpeechletResponseEnvelope();
    response.setVersion(AlexaResource.class.getPackage().getImplementationVersion());
    SpeechletResponse sr = new SpeechletResponse();
    response.setResponse(sr);
    response.setSessionAttributes(request.getSession().getAttributes());
    return response;
  }
}

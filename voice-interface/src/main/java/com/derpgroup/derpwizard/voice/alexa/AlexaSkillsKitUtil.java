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

package com.derpgroup.derpwizard.voice.alexa;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;

/**
 * Utilities for working with Alexa Skills Kit's sometimes-wonky design
 *
 * @author Eric
 * @since 0.0.1
 */
public class AlexaSkillsKitUtil {

  public static AlexaRequestType getRequestType(SpeechletRequest sr){
    if(sr instanceof LaunchRequest){
      return AlexaRequestType.LAUNCH_REQUEST;
    }else if(sr instanceof IntentRequest){
      return AlexaRequestType.INTENT_REQUEST;
    }else if(sr instanceof SessionEndedRequest){
      return AlexaRequestType.SESSION_ENDED_REQUEST;
    }else{
      throw new RuntimeException("Unknown request type."); //TODO: Make an exception for Alexa errors
    }
  }
}

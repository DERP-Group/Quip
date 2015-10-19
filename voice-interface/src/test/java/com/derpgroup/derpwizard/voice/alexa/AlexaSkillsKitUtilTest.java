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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SpeechletRequest;

public class AlexaSkillsKitUtilTest {

  @Test
  public void testGetRequestType(){
    String requestId = "1";
    LaunchRequest launchRequest = LaunchRequest.builder().withRequestId(requestId).build();
    IntentRequest intentRequest = IntentRequest.builder().withRequestId(requestId).build();
    SessionEndedRequest sessionEndedRequest = SessionEndedRequest.builder().withRequestId(requestId).build();
    AlexaRequestType type;
    
    type = AlexaSkillsKitUtil.getRequestType(launchRequest);
    assertEquals(AlexaRequestType.LaunchRequest, type);
    
    type = AlexaSkillsKitUtil.getRequestType(intentRequest);
    assertEquals(AlexaRequestType.IntentRequest, type);
    
    type = AlexaSkillsKitUtil.getRequestType(sessionEndedRequest);
    assertEquals(AlexaRequestType.SessionEndedRequest, type);
  }

  @Test(expected = RuntimeException.class)
  public void testGetRequestType_unknownType(){
    MockRequest mockRequest = new MockRequest("1",new Date());
    AlexaSkillsKitUtil.getRequestType(mockRequest);
  }
  
  private class MockRequest extends SpeechletRequest{

    protected MockRequest(String requestId, Date timestamp) {
      super(requestId, timestamp);
    }
    
  }
}

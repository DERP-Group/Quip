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

package com.derpgroup.derpwizard.voice.model;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.derpgroup.derpwizard.voice.model.VoiceMessageFactory.InterfaceType;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VoiceMessageFactoryTest {
  @Mock com.amazon.speech.speechlet.IntentRequest mockAlexaRequest;

  @Before
  public void before() throws Exception {
    com.amazon.speech.slu.Intent intent = com.amazon.speech.slu.Intent.builder().withName("test").withSlots(Collections.emptyMap()).build();
    when(mockAlexaRequest.getIntent()).thenReturn(intent);
  }

  @Test
  public void buildOutputMessages() throws Exception {
    SsmlDocument doc = new SsmlDocument("<speak><s>hello!</s></speak>");

    // Verify we can create an empty output container for each InterfaceType
    for (InterfaceType type : InterfaceType.values()) {
      VoiceOutput<?> output = VoiceMessageFactory.buildOutputMessage(doc, type);

      assertNotNull(type + " result was null" + type, output);
    }
  }

  @Test
  public void buildAlexaInput() throws Exception {
    // Unit under test
    VoiceInput result = VoiceMessageFactory.buildInputMessage(mockAlexaRequest, InterfaceType.ALEXA);

    // Verify results
    assertNotNull(InterfaceType.ALEXA + " result was null", result);
  }
}

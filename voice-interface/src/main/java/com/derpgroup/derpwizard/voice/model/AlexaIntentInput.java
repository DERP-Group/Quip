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

import java.util.Map.Entry;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;

class AlexaIntentInput implements VoiceInput {
  private IntentRequest request;

  public AlexaIntentInput(Object object) {
    if (!(object instanceof IntentRequest)) {
      throw new IllegalArgumentException("Argument is not an instance of IntentRequest: " + object);
    }

    request = (IntentRequest) object;
  }

  @Override
  public String getMessage() {
    if (request.getIntent().getSlots() == null) {
      return request.getIntent().getName();
    }

    StringBuilder buffer = new StringBuilder(request.getIntent().getName());
    for (Entry<String, Slot> entry : request.getIntent().getSlots().entrySet()) {
      buffer.append(' ');
      buffer.append(entry.getValue().getValue());
    }

    return buffer.toString();
  }
}

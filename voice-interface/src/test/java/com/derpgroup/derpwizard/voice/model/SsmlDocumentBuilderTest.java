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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Before;

public class SsmlDocumentBuilderTest {
  SsmlDocumentBuilder builder;

  @Before
  public void before() throws Exception {
    builder = new SsmlDocumentBuilder();
  }

  @Test
  public void buildSentence() throws Exception {
    String text = "The quick brown fox jumps over the lazy dog";
    String expected = "<speak><p><s>" + text + "</s></p></speak>";

    SsmlDocument doc = builder.normal(text).build();

    assertEquals(expected, doc.getSsml());
  }

  @Test
  public void buildSentenceWithEmphasis() throws Exception {
    SsmlDocument doc = builder.normal("w1 ").strong("w2").normal(" w3").build();

    assertEquals("<speak><p><s>w1 <emphasis level=\"strong\">w2</emphasis> w3</s></p></speak>", doc.getSsml());
  }

  @Test
  public void buildMultipleSentences() throws Exception {
    SsmlDocument doc = builder.normal("Sentence 1").endSentence().normal("Sentence 2").endSentence().normal("Sentence 3").build();

    assertEquals("<speak><p><s>Sentence 1</s><s>Sentence 2</s><s>Sentence 3</s></p></speak>", doc.getSsml());
  }

  @Test
  public void buildMultipleParagraphs() throws Exception {
    SsmlDocument doc = builder.paragraph().normal("Paragraph 1a").endSentence().normal("Paragraph 1b").paragraph().normal("Paragraph 2").paragraph().normal("Paragraph 3").build();

    assertEquals("<speak><p><s>Paragraph 1a</s><s>Paragraph 1b</s></p><p><s>Paragraph 2</s></p><p><s>Paragraph 3</s></p></speak>", doc.getSsml());
  }
}

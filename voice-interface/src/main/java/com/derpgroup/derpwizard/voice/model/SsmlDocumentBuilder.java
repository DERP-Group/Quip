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

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Builder for SsmlDocument objects.
 *
 * @author Rusty
 * @since 0.0.1
 * @see SsmlDocument
 */
public class SsmlDocumentBuilder {
  private List<List<StringBuilder>> paragraphs;
  private int index = 0;

  public SsmlDocumentBuilder() {
    paragraphs = new ArrayList<List<StringBuilder>>();
    paragraphs.add(buildParagraph());
  }

  /**
   * Start a new paragraph.
   *
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder paragraph() {
    if (paragraphs.get(index).size() > 1 || paragraphs.get(index).get(0).length() != 0) {
      paragraphs.add(buildParagraph());
      index++;
    }

    return this;
  }

  /**
   * Add plain text words to the current sentence.
   *
   * @param words
   *          The words to add, not null
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder normal(@NonNull String words) {
    getBuilder().append(words);

    return this;
  }

  /**
   * Add strong-emphasis words to the current sentence.
   *
   * @param words
   *          The words to add, not null
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder strong(@NonNull String words) {
    getBuilder().append("<emphasis level=\"strong\">" + words + "</emphasis>");

    return this;
  }

  /**
   * Add moderate-emphasis words to the current sentence.
   *
   * @param words
   *          The words to add, not null
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder moderate(@NonNull String words) {
    getBuilder().append("<emphasis level=\"moderate\">" + words + "</emphasis>");

    return this;
  }

  /**
   * End the current sentence in the paragraph.
   *
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder endSentence() {
    paragraphs.get(index).add(new StringBuilder());

    return this;
  }

  /**
   * Builds the SsmlDocument
   *
   * @return the document, never null
   */
  public @NonNull SsmlDocument build() {
    StringBuilder buffer = new StringBuilder("<speak>");
    for (List<StringBuilder> paragraph : paragraphs) {
      buffer.append("<p>");
      for (StringBuilder sentence : paragraph) {
        if (sentence.length() > 0) {
          buffer.append("<s>" + sentence + "</s>");
        }
      }
      buffer.append("</p>");
    }
    buffer.append("</speak>");

    return new SsmlDocument(buffer.toString());
  }

  private final StringBuilder getBuilder() {
    return paragraphs.get(index).get(paragraphs.get(index).size() - 1);
  }

  private final List<StringBuilder> buildParagraph() {
    List<StringBuilder> paragraph = new ArrayList<StringBuilder>();
    paragraph.add(new StringBuilder());

    return paragraph;
  }
}

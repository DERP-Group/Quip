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
import java.util.Locale;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;

/**
 * Builder for SsmlDocument objects.
 *
 * @author Rusty
 * @since 0.0.1
 * @see SsmlDocument
 */
public class SsmlDocumentBuilder {
  /**
   * Regular expression pattern for time designations.
   * <p>
   * @see <a href="http://www.w3.org/TR/speech-synthesis11/#def_time_designation">http://www.w3.org/TR/speech-synthesis11/#def_time_designation</a>
   */
  @Regex private static final String TIME_PATTERN = "^\\+?[0-9]*\\.?[0-9]+m?s$";

  /**
   * Enumeration of word stress levels.
   *
   * @author Rusty
   * @since 0.0.1
   * @see <a href="http://www.w3.org/TR/speech-synthesis11/#S3.2.2">http://www.w3.org/TR/speech-synthesis11/#S3.2.2</a>
   */
  public enum EmphasisLevel {
    REDUCED, NONE, MODERATE, STRONG;

    @Override
    public String toString() {
      return super.toString().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }
  }

  /**
   * Enumeration of prosodic break strengths.
   *
   * @author Rusty
   * @since 0.0.1
   * @see <a href="http://www.w3.org/TR/speech-synthesis11/#edef_break">http://www.w3.org/TR/speech-synthesis11/#edef_break</a>
   */
  public enum BreakStrength {
    NONE, X_WEAK, WEAK, MEDIUM, STRONG, X_STRONG;

    @Override
    public String toString() {
      return super.toString().replaceAll("_", "-").toLowerCase(Locale.ENGLISH);
    }
  }

  private List<List<StringBuilder>> paragraphs;
  private int index = 0;

  public SsmlDocumentBuilder() {
    paragraphs = new ArrayList<List<StringBuilder>>();
    paragraphs.add(buildParagraph());
  }

  /**
   * End the current paragraph and start a new one.
   *
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder endParagraph() {
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
  public @NonNull SsmlDocumentBuilder text(@NonNull String words) {
    getSentence().append(words);

    return this;
  }

  /**
   * Add text to the current sentence with emphasis.
   *
   * @param words
   *          The words to add, not null
   * @param emphasis
   *          The emphasis level, nullable
   * @return this, for method chaining
   */
  public @NonNull SsmlDocumentBuilder text(@NonNull String words, @Nullable EmphasisLevel emphasis) {
    if (emphasis == null) {
      return text(words);
    }

    getSentence().append("<emphasis level=\"" + emphasis + "\">" + words + "</emphasis>");

    return this;
  }

  /**
   * Add a break element to the current sentence.
   *
   * @param type
   *          The type of pause, nullable
   * @param time
   *          The length of time to pause, nullable
   * @return this, for method chaining
   * @see #TIME_PATTERN
   */
  public @NonNull SsmlDocumentBuilder pause(@Nullable BreakStrength type, @Nullable String time) {
    getSentence().append("<break");

    if (type != null) {
      getSentence().append(" strength=\"" + type + "\"");
    }

    if (time != null && time.matches(TIME_PATTERN)) {
      getSentence().append(" time=\"" + time + "\"");
    }

    getSentence().append("/>");

    return this;
  }

  /**
   * End the current sentence in the paragraph and start a new one.
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

  private final StringBuilder getSentence() {
    return paragraphs.get(index).get(paragraphs.get(index).size() - 1);
  }

  private final List<StringBuilder> buildParagraph() {
    List<StringBuilder> paragraph = new ArrayList<StringBuilder>();
    paragraph.add(new StringBuilder());

    return paragraph;
  }
}

/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.componentviews.requestmatchers;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * @since 3.0
 */
public class PatternParser
{
  public static final String DEFAULT_VARIABLE_REGEXP = "[^/]+";

  private List<Token> tokens = new ArrayList<>();

  private static final List<Character> NONE = Collections.<Character>emptyList();

  public PatternParser(String tokenPattern) {
    checkNotNull(tokenPattern);

    parseTemplate(new StringCharacterIterator(tokenPattern));
  }

  public List<Token> getTokens() {
    return tokens;
  }

  private void parseTemplate(final CharacterIterator iterator) {
    while (true) {
      final char chr = iterator.current();
      switch (chr) {
        case CharacterIterator.DONE:
          return;
        case '{':
          iterator.next();
          parseVariable(iterator);
          break;
        default:
          parseLiteral(iterator);
      }
    }
  }

  private void parseVariable(CharacterIterator iterator) {
    final String varName = readFragment(iterator, asList(':', '}'), asList('\\'), asList(' ', '}'));
    // Consume the final '}' character

    // The iterator is currently pointing to the end character.
    if (iterator.current() == ':') {
      // Skip the ':' character
      iterator.next();
      final String regexp = readFragment(iterator, asList('}'), asList('\\'), NONE);
      tokens.add(new VariableToken(varName, regexp));
    }
    else {
      tokens.add(new VariableToken(varName, DEFAULT_VARIABLE_REGEXP));
    }

    // The iterator should now be pointing to the varname end delimiter.
    Preconditions.checkArgument(iterator.current() == '}',
        "Variable does not end with '}' at position " + iterator.getIndex());
    // Consume it.
    iterator.next();
  }

  private void parseLiteral(CharacterIterator iterator) {
    final String literal = readFragment(iterator, asList('{'), asList('\\'), NONE);
    tokens.add(new LiteralToken(literal));
  }

  /**
   * Reads from the character iterator until either a stop character is reached or the iterator is {@link
   * CharacterIterator#DONE done}. The stop character is not consumed. Escape characters indicate that the
   * following character should not be considered as a stop character.
   *
   * @throws IllegalArgumentException if the fragment isn't at least one character
   */
  static String readFragment(CharacterIterator iterator, List<Character> stopChars,
                             List<Character> escapeChars, List<Character> disallowed)
  {
    StringBuilder b = new StringBuilder();
    boolean escaping = false;
    while (true) {
      final char cha = iterator.current();

      if (cha == CharacterIterator.DONE) {
        if (escaping) {
          throw new IllegalArgumentException(
              "Unexpected end after escape character '" + cha + "' at position " + iterator.getIndex());
        }
        if (b.length() < 1) {
          throw new IllegalArgumentException("Zero-length fragment at position " + iterator.getIndex());
        }
        return b.toString();
      }

      if (escaping) {
        checkAllowed(iterator, disallowed);
        b.append(cha);
        escaping = false;
      }
      else if (escapeChars.contains(cha)) {
        escaping = true;
      }
      else if (stopChars.contains(cha)) {
        return b.toString();
      }
      else {
        checkAllowed(iterator, disallowed);
        b.append(cha);
      }

      iterator.next();
    }
  }

  private static void checkAllowed(CharacterIterator iterator, List<Character> disallowed) {
    if (disallowed.contains(iterator.current())) {
      throw new IllegalArgumentException(
          "Disallowed character " + iterator.current() + " at position " + iterator.getIndex());
    }
  }
}

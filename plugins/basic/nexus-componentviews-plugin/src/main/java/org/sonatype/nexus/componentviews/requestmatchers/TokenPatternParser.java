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
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class TokenPatternParser
{
  private List<Token> tokens = new ArrayList<>();

  private StringBuilder currentToken;

  public TokenPatternParser(String tokenPattern) {
    checkNotNull(tokenPattern);
    resetToken();

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
      iterator.next();
    }
  }

  private void parseVariable(CharacterIterator iterator) {
    while (true) {
      final char cha = iterator.current();

      switch (cha) {
        case '}': // We're done!
          defineVariable();
          return;
        case CharacterIterator.DONE:
          throw new IllegalArgumentException("Unfinished variable.");
        default:
          pushChar(cha);
      }
      iterator.next();
    }
  }

  private void parseLiteral(CharacterIterator iterator) {
    while (true) {
      final char cha = iterator.current();

      switch (cha) {
        case '{': // We've hit a variable, back up and return
          iterator.previous();
          defineLiteral();
          return;
        case '}': // Variable being n
          throw new IllegalArgumentException("Stray } character.");
        case CharacterIterator.DONE:
          defineLiteral();
          return;
        default:
          pushChar(cha);
      }
      iterator.next();
    }
  }

  private void defineVariable() {
    tokens.add(new VariableToken(currentToken.toString()));
    resetToken();
  }

  private void defineLiteral() {
    tokens.add(new LiteralToken(currentToken.toString()));
    resetToken();
  }

  private void pushChar(char character) {
    currentToken.append(character);
  }

  private void resetToken() {
    currentToken = new StringBuilder();
  }

}

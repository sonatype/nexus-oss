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

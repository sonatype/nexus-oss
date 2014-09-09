package org.sonatype.nexus.componentviews.requestmatchers;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public abstract class Token
{
  protected final String value;

  protected Token(final String value) {
    this.value = checkNotNull(value);
  }

  public abstract String toRegexp();

  private static final List<Character> REGEXP_CHARS = Arrays.asList('[', ']', '{', '}', '\\', '.');

  protected String escapeRegexpChars(String input) {
    final CharacterIterator charIterator = new StringCharacterIterator(input);
    final StringBuilder result = new StringBuilder();

    while (true) {
      final char cha = charIterator.current();

      if (cha == CharacterIterator.DONE) {
        return result.toString();

      }
      else if (REGEXP_CHARS.contains(cha)) {
        result.append('\\').append(cha);
      }
      else {
        result.append(cha);
      }
      charIterator.next();
    }
  }
}

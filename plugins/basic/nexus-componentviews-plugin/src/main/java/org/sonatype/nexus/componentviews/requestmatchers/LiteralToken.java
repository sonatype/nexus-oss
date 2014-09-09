package org.sonatype.nexus.componentviews.requestmatchers;

/**
 * @since 3.0
 */
public class LiteralToken
    extends Token
{
  public LiteralToken(final String value) {
    super(value);
  }

  @Override
  public String toRegexp() {
    return escapeRegexpChars(value);
  }
}

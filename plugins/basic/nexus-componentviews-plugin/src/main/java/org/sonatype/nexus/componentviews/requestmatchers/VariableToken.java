package org.sonatype.nexus.componentviews.requestmatchers;

/**
 * @since 3.0
 */
public class VariableToken
    extends Token
{
  public VariableToken(final String value) {
    super(value);
  }

  public String getName() {
    return value;
  }

  @Override
  public String toRegexp() {
    return "(.+)";
  }
}

package org.sonatype.nexus.componentviews.requestmatchers;

import java.util.List;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @since 3.0
 */
public class TokenPatternParserTest
{
  @Test
  public void parseSimplePatterns() {
    checkTokenDetection("asdfasdf", "asdfasdf");
    checkTokenDetection("/{a}", "/(.+)");
    checkTokenDetection("/{a}/{b}", "/(.+)/(.+)");
    checkTokenDetection("{chocolate}/{build}-{version}.{extension}", "(.+)/(.+)-(.+)\\.(.+)");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unfinishedVariableFails() {
    parse("{");
  }

  @Test(expected=IllegalArgumentException.class)
  public void unstartedVariableFails(){
    parse("/hi}");
  }

  private void checkTokenDetection(final String pattern, final String regexpValue) {
    final TokenPatternParser parser = parse(pattern);
    final List<Token> tokens = parser.getTokens();
    StringBuilder regexp = new StringBuilder();
    for (Token token : tokens) {
      regexp.append(token.toRegexp());
    }
    assertThat(regexp.toString(), is(equalTo(regexpValue)));
  }

  private TokenPatternParser parse(final String pattern) {return new TokenPatternParser(pattern);}
}

package org.sonatype.nexus.componentviews.requestmatchers;

import java.util.Map;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @since 3.0
 */
public class TokenMatcherTest
{
  @Test
  public void simplePattern() {
    String pattern = "/{a}/{b}/{c}";

    final TokenMatcher matcher = new TokenMatcher(pattern);

    final Map<String, String> tokens = matcher.matchTokens("/yes/no/false");

    assertThat(tokens, is(notNullValue()));
    assertThat(tokens.size(), is(equalTo(3)));

    assertThat(tokens.get("a"), is(equalTo("yes")));
    assertThat(tokens.get("b"), is(equalTo("no")));
    assertThat(tokens.get("c"), is(equalTo("false")));
  }

  @Test
  public void parsingMaven() {
    final String pattern = "/{group}/{module}/{version}/{name}-{version}.{ext}";
    final TokenMatcher tokenMatcher = new TokenMatcher(pattern);
    final Map<String, String> tokens = tokenMatcher.matchTokens("/foo/bar/1234/bar-1234.zip");

    assertThat(tokens, is(notNullValue()));
    assertThat(tokens.size(), is(equalTo(5)));

    assertThat(tokens.get("group"), is(equalTo("foo")));
    assertThat(tokens.get("module"), is(equalTo("bar")));
    assertThat(tokens.get("version"), is(equalTo("1234")));
    assertThat(tokens.get("name"), is(equalTo("bar")));
    assertThat(tokens.get("ext"), is(equalTo("zip")));
  }

  @Test
  public void testSlashesInMavenGroup(){
    final String pattern = "/{group}/{module}/{version}/{name}-{version}.{ext}";
    final TokenMatcher tokenMatcher = new TokenMatcher(pattern);
    final Map<String, String> tokens = tokenMatcher.matchTokens("/org/sonatype/nexus/components/1234/bar-1234.zip");

    assertThat(tokens, is(notNullValue()));
    assertThat(tokens.size(), is(equalTo(5)));

    // TODO: This works through sheer luck because 'group' is the first variable.

    assertThat(tokens.get("group"), is(equalTo("org/sonatype/nexus")));
    assertThat(tokens.get("module"), is(equalTo("components")));
    assertThat(tokens.get("version"), is(equalTo("1234")));
    assertThat(tokens.get("name"), is(equalTo("bar")));
    assertThat(tokens.get("ext"), is(equalTo("zip")));
  }
}

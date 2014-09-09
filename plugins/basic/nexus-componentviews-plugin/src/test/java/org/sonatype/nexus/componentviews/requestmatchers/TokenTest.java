package org.sonatype.nexus.componentviews.requestmatchers;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @since 3.0
 */
public class TokenTest
{
  @Test
  public void literalToken() {
    checkRegexpForm("/", "/");
    checkRegexpForm("/--a", "/--a");
    checkRegexpForm("[]", "\\[\\]");
  }

  private void checkRegexpForm(final String token, final String regexp) {
    assertThat(new LiteralToken(token).toRegexp(), is(equalTo(regexp)));
  }
}

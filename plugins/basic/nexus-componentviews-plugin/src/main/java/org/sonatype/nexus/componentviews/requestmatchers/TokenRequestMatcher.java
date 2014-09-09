package org.sonatype.nexus.componentviews.requestmatchers;

import java.util.Map;

import org.sonatype.nexus.componentviews.RequestMatcher;
import org.sonatype.nexus.componentviews.ViewRequest;

/**
 * A RequestMatcher that examines the {@link ViewRequest#getPath() request path} and attempts to parse it using the
 * {@link TokenMatcher}. If there is a match, the tokens are stored in the {@link ViewRequest#getAttribute(String)
 * request attribute} {@link #PATH_TOKENS} so that handlers can access them.
 *
 * @since 3.0
 */
public class TokenRequestMatcher
    implements RequestMatcher
{
  private final TokenMatcher tokenMatcher;

  public TokenRequestMatcher(final TokenMatcher tokenMatcher) {this.tokenMatcher = tokenMatcher;}

  public static final String PATH_TOKENS = "TokenRequestMatcher.pathTokens";

  @Override
  public boolean matches(final ViewRequest request) {
    final Map<String, String> tokenMap = tokenMatcher.matchTokens(request.getPath());

    if (tokenMap == null) {
      // There was no match.
      return false;
    }

    request.setAttribute(PATH_TOKENS, tokenMap);
    return true;
  }
}

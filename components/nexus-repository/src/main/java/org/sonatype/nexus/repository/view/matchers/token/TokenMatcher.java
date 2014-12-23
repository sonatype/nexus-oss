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
package org.sonatype.nexus.repository.view.matchers.token;

import java.util.Map;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Request;

/**
 * A {@link Matcher} that examines the {@link Request#getPath() request path} and attempts to parse it using the
 * {@link TokenMatcher}. If there is a match, the tokens are stored in the {@link Context#get(String)}
 * request attribute} {@link #PATH_TOKENS} so that handlers can access them.
 *
 * @since 3.0
 */
public class TokenMatcher
    implements Matcher
{
  public static final String PATH_TOKENS = TokenMatcher.class.getName() + ".pathTokens";

  private final TokenMatcherInternal tokenMatcher;

  public TokenMatcher(final String pattern) {
    this.tokenMatcher = new TokenMatcherInternal(pattern);
  }
  
  @Override
  public boolean matches(final Context context) {
    final Map<String, String> tokenMap = findTokens(context.getRequest());

    if (tokenMap == null) {
      // There was no match.
      return false;
    }

    context.set(PATH_TOKENS, tokenMap);
    return true;
  }

  private Map<String, String> findTokens(final Request request) {
    return tokenMatcher.matchTokens(request.getPath());
  }
}

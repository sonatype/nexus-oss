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

  public TokenRequestMatcher(final TokenMatcher tokenMatcher) {
    this.tokenMatcher = tokenMatcher;
  }

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

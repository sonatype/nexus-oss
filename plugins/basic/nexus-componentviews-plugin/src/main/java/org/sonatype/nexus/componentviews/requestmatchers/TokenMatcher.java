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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Examines a string...
 *
 * TODO: Document the format.
 *
 * By default, variables are matched against the regexp {@link TokenPatternParser#DEFAULT_VARIABLE_REGEXP}.
 *
 * @since 3.0
 */
public class TokenMatcher
{
  private final List<VariableToken> variables;

  private final Pattern pattern;

  public TokenMatcher(final String templatePattern) {
    final List<Token> tokens = new TokenPatternParser(templatePattern).getTokens();
    pattern = Pattern.compile(regexp(tokens));

    // Separate the variable tokens
    variables = new ArrayList<>();
    for (Token token : tokens) {
      if (token instanceof VariableToken) {
        variables.add((VariableToken) token);
      }
    }
  }

  public Map<String, String> matchTokens(String path) {
    final Matcher matcher = pattern.matcher(path);
    if (!matcher.matches()) {
      return null;
    }

    if (matcher.groupCount() != variables.size()) {
      throw new IllegalStateException(
          "Mismatch between the number of captured groups (" + matcher.groupCount() + " and the number of variables, " +
              variables.size() + ".");
    }


    Map<String, String> values = new HashMap<>();
    for (int i = 0; i < matcher.groupCount(); i++) {
      values.put(variables.get(i).getName(), matcher.group(i + 1));
    }

    return values;
  }

  private String regexp(List<Token> tokens) {
    StringBuilder b = new StringBuilder();
    for (Token token : tokens) {
      b.append(token.toRegexp());
    }
    return b.toString();
  }
}

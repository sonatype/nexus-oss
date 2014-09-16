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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;


/**
 * Parses path-like strings against a template pattern, a literal string with embedded variables. e.g. {@code
 * "/{binName}/{bucketName}"} would match {@code "/bin12/overages"}.
 *
 * By default, variables are matched against the regexp {@link PatternParser#DEFAULT_VARIABLE_REGEXP}, which
 * excludes the path separator ({@code '/'}). To override this, specify the regexp explicitly:
 *
 * e.g. {@code "/{group:.+}/{artifact}/{version}/{name}-{version}.{ext}"}
 *
 * {@code "{letterCode:[A-Za-z]}"}<
 *
 * The backslash ({@code '\'}) serves as an escape character, if (say) it is necessary to use curly braces in the
 * regular expression.  If escape characters appear in regular expressions, these must be double-escaped.
 *
 * Caveat: the {@link TokenMatcher} cannot handle groups in variable regexp definitions. This will cause
 * parsing errors.
 *
 * @since 3.0
 */
public class TokenMatcher
{
  private final List<VariableToken> variables;

  private final Pattern pattern;

  public TokenMatcher(final String templatePattern) {
    final List<Token> tokens = new PatternParser(templatePattern).getTokens();
    pattern = Pattern.compile(regexp(tokens));

    // Separate the variable tokens
    variables = new ArrayList<>();
    for (Token token : tokens) {
      if (token instanceof VariableToken) {
        variables.add((VariableToken) token);
      }
    }
  }

  /**
   * Attempts to parse the provided path against the template pattern.  If the pattern matches, the resulting Map
   * contains an entry for each variable in the pattern. The variable names are keys, with the matching portions of the
   * path as values.  Returns {@code null} if the pattern does not match.
   */
  @Nullable
  public Map<String, String> matchTokens(String path) {
    final Matcher matcher = pattern.matcher(path);
    if (!matcher.matches()) {
      return null;
    }

    checkState(matcher.groupCount() == variables.size(),
        "Mismatch between the number of captured groups (%s) and the number of variables, %s.", matcher.groupCount(),
        variables.size());

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

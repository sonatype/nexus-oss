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

  @Test
  public void parseRegexp(){
    checkTokenDetection
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

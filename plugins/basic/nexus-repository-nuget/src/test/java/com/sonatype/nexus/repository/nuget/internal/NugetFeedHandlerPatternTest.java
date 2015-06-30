/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal;

import java.util.Map;

import org.sonatype.nexus.repository.view.matchers.token.TokenParser;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NugetFeedHandlerPatternTest
{
  @Test
  public void feedRoute() {
    final TokenParser tokenParser = new TokenParser(NugetFeedHandler.FEED_PATTERN);

    String path = "/Search()";
    assertThat(tokenParser.parse(path), is(Matchers.notNullValue()));
  }

  @Test
  public void feedCountRoute() {
    final TokenParser tokenParser = new TokenParser(NugetFeedHandler.FEED_COUNT_PATTERN);

    String path = "/Search()/$count";
    assertThat(tokenParser.parse(path), is(Matchers.notNullValue()));
  }

  @Test
  public void packageEntryRoute() {
    final TokenParser tokenParser = new TokenParser(NugetFeedHandler.PACKAGE_ENTRY_PATTERN);

    String path = "/Packages(Id='fire',Version='1.1.2-beta')";
    final Map<String, String> tokens = tokenParser.parse(path);
    assertThat(tokens, is(Matchers.notNullValue()));

    assertThat(tokens.get("id"), is("fire"));
    assertThat(tokens.get("version"), is("1.1.2-beta"));
  }
}
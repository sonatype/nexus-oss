/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import org.sonatype.nexus.repository.view.matchers.token.TokenParser;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NugetRouteMatchingTest
{
  @Test
  public void feedRoute() {
    final TokenParser tokenParser = new TokenParser(NugetHostedRecipe.FEED_PATTERN);

    String search = "/Search()";
    assertThat(tokenParser.parse(search), is(Matchers.notNullValue()));
  }

  @Test
  public void feedCountRoute(){
    final TokenParser tokenParser = new TokenParser(NugetHostedRecipe.FEED_COUNT_PATTERN);

    String search = "/Search()/$count";
    assertThat(tokenParser.parse(search), is(Matchers.notNullValue()));
  }
}
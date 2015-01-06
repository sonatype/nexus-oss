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
package org.sonatype.nexus.repository.httpbridge.internal;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link RepositoryPath}.
 */
public class RepositoryPathTest
    extends TestSupport
{
  @Test
  public void nullPath() {
    RepositoryPath parsedPath = RepositoryPath.parse(null);
    assertThat(parsedPath, nullValue());
  }

  @Test
  public void emptyPath() {
    checkPaths("", "", "");
  }

  @Test
  public void bareSlash() {
    checkPaths("/", "", "");
  }

  @Test
  public void repoOnly() {
    checkPaths("/repo", "repo", "");
  }

  @Test
  public void repoOnlyWithFinalSlash() {
    checkPaths("/repo/", "repo", "/");
  }

  @Test
  public void repoOnlyWithTwoFinalSlashes() {
    checkPaths("/repo//", "repo", "//");
  }

  @Test
  public void noRepo() {
    checkPaths("//path", "", "/path");
  }

  private void checkPaths(final String path, final String expectedRepoName, final String expectedRemainingPath) {
    final RepositoryPath parsedPath = RepositoryPath.parse(path);
    assertThat(parsedPath, notNullValue());
    assertThat(parsedPath.getRepositoryName(), is(expectedRepoName));
    assertThat(parsedPath.getRemainingPath(), is(expectedRemainingPath));
  }
}
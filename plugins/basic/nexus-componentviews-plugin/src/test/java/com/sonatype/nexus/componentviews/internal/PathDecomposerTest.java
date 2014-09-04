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
package com.sonatype.nexus.componentviews.internal;

import org.sonatype.nexus.componentviews.internal.PathDecomposer;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @since 3.0
 */
public class PathDecomposerTest
{
  @Test
  public void nullPath() {
    checkPaths(null, "", "/");
  }

  @Test
  public void emptyPath() {
    checkPaths("", "", "/");
  }

  @Test
  public void bareSlash() {
    checkPaths("/", "", "/");
  }

  @Test
  public void viewOnly() {
    checkPaths("/viewname", "viewname", "/");
  }

  @Test
  public void viewOnlyWithFinalSlash() {
    checkPaths("/viewname/", "viewname", "/");
  }

  @Test
  public void viewWithExtraPath() {
    checkPaths("/viewname/plusDetails/foo", "viewname", "/plusDetails/foo");
  }

  private void checkPaths(final String path, final String viewName, final String remainingPath) {
    final PathDecomposer pathDecomposer = new PathDecomposer(path);

    assertThat(pathDecomposer.getViewName(), is(equalTo(viewName)));
    assertThat(pathDecomposer.getRemainingPath(), is(equalTo(remainingPath)));
  }
}

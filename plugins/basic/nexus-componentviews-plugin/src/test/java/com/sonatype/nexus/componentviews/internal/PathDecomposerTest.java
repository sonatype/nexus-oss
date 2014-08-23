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

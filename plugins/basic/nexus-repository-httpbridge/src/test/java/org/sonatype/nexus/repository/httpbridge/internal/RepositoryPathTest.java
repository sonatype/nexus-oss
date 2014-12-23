package org.sonatype.nexus.repository.httpbridge.internal;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RepositoryPathTest
    extends TestSupport
{
  @Test
  public void nullPath() {
    checkPaths(null, "", "");
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

  private void checkPaths(final String path, final String expectedRepoKey, final String expectedRemainingPath) {
    final RepositoryPath repoKeyParser = RepositoryPath.parse(path);

    assertThat(repoKeyParser.getRepositoryKey(), is(equalTo(expectedRepoKey)));
    assertThat(repoKeyParser.getRemainingPath(), is(equalTo(expectedRemainingPath)));
  }
}
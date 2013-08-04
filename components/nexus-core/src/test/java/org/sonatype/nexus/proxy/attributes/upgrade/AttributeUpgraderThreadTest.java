/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.attributes.upgrade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.maven.MavenShadowRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.util.NumberSequence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AttributeUpgraderThread}
 *
 * @since 2.0.5.1
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeUpgraderThreadTest
{

  @Mock
  private File legacyAttributesDirectory;

  @Mock
  private RepositoryRegistry repositoryRegistry;

  @Mock
  private NumberSequence numberSequence;

  @Mock
  private RepositoryKind repoKind;

  @Mock
  private Repository repo;

  /**
   * NEXUS-5099 skip shadow repos when performing attributes upgrade
   */
  @Test
  public void shouldNotUpgradeShadowRepos() {
    final AttributeUpgraderThread aut =
        new AttributeUpgraderThread(legacyAttributesDirectory, repositoryRegistry, 1, numberSequence);

    when(repo.getRepositoryKind()).thenReturn(repoKind);

    when(repoKind.isFacetAvailable(MavenShadowRepository.class)).thenReturn(true);
    assertThat(aut.shouldUpgradeRepository(repo), is(false));

    when(repoKind.isFacetAvailable(MavenShadowRepository.class)).thenReturn(false);
    assertThat(aut.shouldUpgradeRepository(repo), is(true));

  }

  @Test
  public void shouldNotUpgradeGroupRepos() {
    final AttributeUpgraderThread aut =
        new AttributeUpgraderThread(legacyAttributesDirectory, repositoryRegistry, 1, numberSequence);

    when(repo.getRepositoryKind()).thenReturn(repoKind);

    when(repoKind.isFacetAvailable(GroupRepository.class)).thenReturn(true);
    assertThat(aut.shouldUpgradeRepository(repo), is(false));

    when(repoKind.isFacetAvailable(GroupRepository.class)).thenReturn(false);
    assertThat(aut.shouldUpgradeRepository(repo), is(true));

  }

  private Repository prepRepoKind(Repository repo) {
    when(repo.getRepositoryKind()).thenReturn(repoKind);
    return repo;
  }

  /**
   * Make sure run() makes the right decisions regarding whether to upgrade a repo
   */
  @Test
  public void runDelegatesCallsToHelpers()
      throws Exception
  {
    Repository repo1 = mock(Repository.class);
    Repository repo2 = mock(Repository.class);
    List<Repository> repos = new ArrayList<Repository>(2);
    repos.add(prepRepoKind(repo1));
    repos.add(prepRepoKind(repo2));
    when(repositoryRegistry.getRepositories()).thenReturn(repos);

    final AttributeUpgraderThread aut =
        spy(new AttributeUpgraderThread(legacyAttributesDirectory, repositoryRegistry, 1, numberSequence));

    doReturn(false).when(aut).shouldUpgradeRepository(repo1);
    doReturn(false).when(aut).shouldUpgradeRepository(repo2);
    // force run to continue and avoid nasty static stubbing
    doReturn(false).when(aut).isUpgradeDone(null);
    // prevent nasty static call
    doNothing().when(aut).markUpgradeDone(anyString());
    // so we do not slow down test with sleep
    doNothing().when(aut).throttleRun();

    aut.run();

    // make sure stubs were checked
    verify(aut).throttleRun();
    verify(aut).shouldUpgradeRepository(repo1);
    verify(aut).shouldUpgradeRepository(repo2);
    verify(aut).isUpgradeDone(null);

  }

}

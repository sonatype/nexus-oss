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
package org.sonatype.nexus.yum.internal.task;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.routing.Manager;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.yum.Yum;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.nexus.yum.internal.support.YumNexusTestSupport;

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class GenerateMetadataTaskSettingsIT
    extends YumNexusTestSupport
{

  private static final String ANOTHER_REPO = "repo2";

  private static final String ANOTHER_VERSION = "version2";

  private static final String VERSION = "version";

  private static final String NO_VERSION = null;

  private static final String REPO = "REPO1";

  private static final String BASE_URL = "http://foo.bla";

  private static final String RPM_URL = BASE_URL + "/content/repositories/" + REPO;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void shouldNotExecuteIfOperateOnSameRepository()
      throws Exception
  {
    GenerateMetadataTask task = task(REPO, NO_VERSION);
    assertFalse(task.isBlockedBy(asList(scheduledTask(REPO, NO_VERSION))).isEmpty());
  }

  @Test
  public void shouldNotExecuteIfOperateOnSameRepositoryAndSameVersion()
      throws Exception
  {
    GenerateMetadataTask task = task(REPO, VERSION);
    assertFalse(task.isBlockedBy(asList(scheduledTask(REPO, VERSION))).isEmpty());
  }

  @Test
  public void shouldExecuteIfOperateOnSameRepositoryAndAnotherVersion()
      throws Exception
  {
    GenerateMetadataTask task = task(REPO, VERSION);
    assertTrue(task.isBlockedBy(asList(scheduledTask(REPO, ANOTHER_VERSION))).isEmpty());
  }

  @Test
  public void shouldExecuteIfOperateOnAnotherRepository()
      throws Exception
  {
    GenerateMetadataTask task = task(REPO, NO_VERSION);
    assertTrue(task.isBlockedBy(asList(scheduledTask(ANOTHER_REPO, NO_VERSION))).isEmpty());
  }

  @Test
  public void shouldSetDefaultsForRepoParams()
      throws Exception
  {
    // given
    GenerateMetadataTask task = new GenerateMetadataTask(
        mock(YumRegistry.class),
        mock(RpmScanner.class),
        mock(Manager.class),
        mock(CommandLineExecutor.class)
    );
    task.setRepositoryRegistry(repoRegistry());
    task.setRpmDir(rpmsDir().getAbsolutePath());
    // when
    task.setDefaults();
    // then
    assertThat(task.getRepoDir(), is(rpmsDir().getAbsoluteFile()));
  }

  @Test
  public void shouldSetDefaultsIfOnlyRepoWasSet()
      throws Exception
  {
    // given
    GenerateMetadataTask task = new GenerateMetadataTask(
        mock(YumRegistry.class),
        mock(RpmScanner.class),
        mock(Manager.class),
        mock(CommandLineExecutor.class)
    );
    task.setRepositoryRegistry(repoRegistry());
    task.setRepositoryId(REPO);
    // when
    task.setDefaults();
    // then
    assertThat(task.getRpmDir(), is(rpmsDir().getAbsolutePath()));
    assertThat(task.getRepoDir(), is(rpmsDir().getAbsoluteFile()));
  }

  @Test
  public void shouldNotExecuteOnRepositoriesThatAreNotRegistered()
      throws Exception
  {
    GenerateMetadataTask task = new GenerateMetadataTask(
        mock(YumRegistry.class),
        mock(RpmScanner.class),
        mock(Manager.class),
        mock(CommandLineExecutor.class)
    );
    task.setRepositoryRegistry(repoRegistry());
    task.setRepositoryId(REPO);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("enabled 'Yum: Generate Metadata' capability");
    task.execute();
  }

  @Test
  public void shouldNotExecuteOnNonMavenHostedRepository()
      throws Exception
  {
    RepositoryKind repositoryKind = mock(RepositoryKind.class);
    when(repositoryKind.isFacetAvailable(HostedRepository.class)).thenReturn(false);
    Repository repository = mock(Repository.class);
    when(repository.getRepositoryKind()).thenReturn(repositoryKind);
    Yum yum = mock(Yum.class);
    when(yum.getNexusRepository()).thenReturn(repository);
    YumRegistry yumRegistry = mock(YumRegistry.class);
    when(yumRegistry.isRegistered(REPO)).thenReturn(true);
    when(yumRegistry.get(REPO)).thenReturn(yum);

    GenerateMetadataTask task = new GenerateMetadataTask(
        yumRegistry,
        mock(RpmScanner.class),
        mock(Manager.class),
        mock(CommandLineExecutor.class)
    );
    task.setRepositoryRegistry(repoRegistry());
    task.setRepositoryId(REPO);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("hosted repositories");
    task.execute();
  }

  private RepositoryRegistry repoRegistry()
      throws Exception
  {
    final Repository repo = mock(Repository.class);
    when(repo.getId()).thenReturn(REPO);
    when(repo.getLocalUrl()).thenReturn(osIndependentUri(rpmsDir()));
    final RepositoryRegistry repoRegistry = mock(RepositoryRegistry.class);
    when(repoRegistry.getRepository(anyString())).thenReturn(repo);
    when(repoRegistry.getRepositoryWithFacet(anyString(), eq(GroupRepository.class))).thenThrow(
        new NoSuchRepositoryException(REPO));
    return repoRegistry;
  }

  private List<TaskInfo<?>> asList(TaskInfo<?> task) {
    return ImmutableList.<TaskInfo<?>>of(task);
  }

  private TaskInfo<YumRepository> scheduledTask(String repo, String version) {
    final TaskConfiguration configuration = new TaskConfiguration();
    configuration.setId("id");
    configuration.setName("name");
    configuration.setTypeId(GenerateMetadataTask.class.getSimpleName());
    configuration.setRepositoryId(repo);
    configuration.setString(GenerateMetadataTask.PARAM_VERSION, version);
    TaskInfo task = mock(TaskInfo.class);
    when(task.getConfiguration()).thenReturn(configuration);
    when(task.getId()).thenReturn(configuration.getId());
    CurrentState currentState = mock(CurrentState.class);
    when(currentState.getState()).thenReturn(State.RUNNING);
    when(task.getCurrentState()).thenReturn(currentState);
    return task;
  }

  private GenerateMetadataTask task(String repo, String version) throws Exception {
    final YumRegistry yumRegistry = mock(YumRegistry.class);
    when(yumRegistry.maxNumberOfParallelThreads()).thenReturn(YumRegistry.DEFAULT_MAX_NUMBER_PARALLEL_THREADS);

    GenerateMetadataTask task = new GenerateMetadataTask(
        yumRegistry,
        mock(RpmScanner.class),
        mock(Manager.class),
        mock(CommandLineExecutor.class)
    )
    {
      @Override
      protected YumRepository execute()
          throws Exception
      {
        return null;
      }
    };
    TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId("foo");
    taskConfiguration.setTypeId(GenerateMetadataTask.class.getSimpleName());
    task.configure(taskConfiguration);
    task.setRepositoryRegistry(repoRegistry());
    task.setRpmDir(rpmsDir().getAbsolutePath());
    task.setRepoDir(rpmsDir());
    task.setRepositoryId(repo);
    task.setVersion(version);
    task.setAddedFiles(null);
    task.setSingleRpmPerDirectory(true);

    return task;
  }
}

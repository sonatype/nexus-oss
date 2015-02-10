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
package org.sonatype.nexus.yum.internal.task;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.internal.support.YumNexusTestSupport;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MergeMetadataTaskIT
    extends YumNexusTestSupport
{

  private static final String GROUP_ID_1 = "group-repo-id-1";

  private static final String GROUP_ID_2 = "group-repo-id-2";

  @Test
  public void shouldNotAllowConcurrentExecutionForSameRepo()
      throws Exception
  {
    final MergeMetadataTask task = new MergeMetadataTask(mock(YumRegistry.class));
    TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId("foo");
    taskConfiguration.setTypeId(MergeMetadataTask.class.getSimpleName());
    taskConfiguration.setRepositoryId(GROUP_ID_1);
    task.configure(taskConfiguration);
    task.setRepositoryRegistry(repoRegistry());
    assertThat(task.isBlockedBy(createRunningTaskForGroups(GROUP_ID_1)).isEmpty(), is(false));
  }

  @Test
  public void shouldAllowConcurrentExecutionIfAnotherTaskIsRunning()
      throws Exception
  {
    final MergeMetadataTask task = new MergeMetadataTask(mock(YumRegistry.class));
    TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId("foo");
    taskConfiguration.setTypeId(MergeMetadataTask.class.getSimpleName());
    taskConfiguration.setRepositoryId(GROUP_ID_1);
    task.configure(taskConfiguration);
    task.setRepositoryRegistry(repoRegistry());
    assertThat(task.isBlockedBy(createRunningTaskForGroups(GROUP_ID_2)).isEmpty(), is(true));
  }

  private RepositoryRegistry repoRegistry()
      throws Exception
  {
    final RepositoryRegistry repoRegistry = mock(RepositoryRegistry.class);
    when(repoRegistry.getRepositoryWithFacet(anyString(), eq(GroupRepository.class))).thenThrow(
        new NoSuchRepositoryException("foo"));
    return repoRegistry;
  }

  private List<TaskInfo<?>> createRunningTaskForGroups(final String... groupIds) {
    final List<TaskInfo<?>> taskList = Lists.newArrayList();
    for (final String groupId : groupIds) {
      taskList.add(runningTask(groupId));
    }
    return taskList;
  }

  private TaskInfo<?> runningTask(final String repoId) {
    final TaskInfo<?> task = mock(TaskInfo.class);
    final TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId("foo");
    taskConfiguration.setTypeId(MergeMetadataTask.class.getSimpleName());
    taskConfiguration.setRepositoryId(repoId);
    when(task.getConfiguration()).thenReturn(taskConfiguration);
    CurrentState currentState = mock(CurrentState.class);
    when(currentState.getState()).thenReturn(State.RUNNING);
    when(task.getCurrentState()).thenReturn(currentState);
    return task;
  }

}

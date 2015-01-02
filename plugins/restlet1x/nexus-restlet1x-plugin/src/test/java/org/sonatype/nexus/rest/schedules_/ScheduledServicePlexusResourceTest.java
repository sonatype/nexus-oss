/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.schedules_;

import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.EndState;
import org.sonatype.nexus.scheduling.TaskInfo.LastRunState;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

public class ScheduledServicePlexusResourceTest
    extends TestSupport
{

  @Test
  @Ignore("Unreal test, no assertion")
  public void testGetReadableState() {
    AbstractScheduledServicePlexusResource service = new AbstractScheduledServicePlexusResource()
    {

      @Override
      public String getResourceUri() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public PathProtectionDescriptor getResourceProtection() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Object getPayloadInstance() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    // TaskState[] states = TaskState.values();
    // for (TaskState state : states) {
    //   service.getReadableState(state);
    // }
  }

  @Test
  public void testGetLastRunResult() {
    AbstractScheduledServicePlexusResource service = new AbstractScheduledServicePlexusResource()
    {
      @Override
      public String getResourceUri() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public PathProtectionDescriptor getResourceProtection() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Object getPayloadInstance() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    TaskInfo<?> task = Mockito.mock(TaskInfo.class);
    LastRunState lastRunState = Mockito.mock(LastRunState.class);
    when(task.getLastRunState()).thenReturn(lastRunState);
    when(lastRunState.getEndState()).thenReturn(EndState.OK);
    when(lastRunState.getRunDuration()).thenReturn(58L * 1000L);
    assertThat(service.getLastRunResult(task), equalTo("Ok [58s]"));

    when(lastRunState.getRunDuration()).thenReturn(7L * 60L * 1000L);
    assertThat(service.getLastRunResult(task), equalTo("Ok [7m0s]"));

    when(lastRunState.getRunDuration()).thenReturn(3L * 60L * 60L * 1000L);
    assertThat(service.getLastRunResult(task), equalTo("Ok [3h0m0s]"));

    when(lastRunState.getRunDuration()).thenReturn(
        2L * 24L * 60L * 60L * 1000L + 5L * 60L * 60L * 1000L + 13L * 60L * 1000L + 22L * 1000L);
    assertThat(service.getLastRunResult(task), equalTo("Ok [53h13m22s]"));
  }

}

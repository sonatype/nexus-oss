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
package org.sonatype.nexus.scheduling.internal;

import java.util.Collections;

import javax.inject.Named;

import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptor;
import org.sonatype.nexus.scheduling.internal.Tasks.TaskWithDescriptor;
import org.sonatype.nexus.scheduling.internal.Tasks.TaskWithDescriptorDescriptor;
import org.sonatype.nexus.scheduling.internal.Tasks.TaskWithoutDescriptor;
import org.sonatype.nexus.scheduling.spi.NexusTaskExecutorSPI;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableList;
import org.eclipse.sisu.BeanEntry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.sonatype.nexus.scheduling.internal.Tasks.beanEntry;

public class DefaultNexusTaskSchedulerTest
    extends TestSupport
{
  @Test
  public void createTaskConfigurationInstance() {
    final BeanEntry<Named, Task> be1 = beanEntry(TaskWithDescriptor.class);
    final BeanEntry<Named, Task> be2 = beanEntry(TaskWithoutDescriptor.class);
    final DefaultNexusTaskFactory nexusTaskFactory = new DefaultNexusTaskFactory(
        ImmutableList.of(be1, be2));
    final DefaultNexusTaskScheduler nexusTaskScheduler = new DefaultNexusTaskScheduler(nexusTaskFactory,
        ImmutableList.<TaskDescriptor>of(new TaskWithDescriptorDescriptor()),
        Collections.<NexusTaskExecutorSPI>emptyList());


    final TaskConfiguration c1 = nexusTaskScheduler.createTaskConfigurationInstance(TaskWithDescriptor.class);
    assertThat(c1.getId(), notNullValue());
    assertThat(c1.getName(), equalTo(TaskWithDescriptor.class.getSimpleName()));
    assertThat(c1.getType(), equalTo(TaskWithDescriptor.class.getName()));
    assertThat(c1.isVisible(), is(true));
    assertThat(c1.isEnabled(), is(true));

    final TaskConfiguration c2 = nexusTaskScheduler.createTaskConfigurationInstance(TaskWithoutDescriptor.class);
    assertThat(c2.getId(), notNullValue());
    assertThat(c2.getName(), equalTo(TaskWithoutDescriptor.class.getName()));
    assertThat(c2.getType(), equalTo(TaskWithoutDescriptor.class.getName()));
    assertThat(c2.isVisible(), is(false));
    assertThat(c2.isEnabled(), is(true));

    try {
      final TaskConfiguration c3 = nexusTaskScheduler.createTaskConfigurationInstance("foobar");
      fail("This should not return");
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), containsString("foobar"));
    }
  }
}

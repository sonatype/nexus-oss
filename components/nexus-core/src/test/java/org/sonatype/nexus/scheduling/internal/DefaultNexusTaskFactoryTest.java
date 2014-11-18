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

import javax.inject.Named;

import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.internal.Tasks.TaskWithDescriptor;
import org.sonatype.nexus.scheduling.internal.Tasks.TaskWithoutDescriptor;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableList;
import org.eclipse.sisu.BeanEntry;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.sonatype.nexus.scheduling.internal.Tasks.beanEntry;

public class DefaultNexusTaskFactoryTest
    extends TestSupport
{
  @Test
  public void isTask() {
    final BeanEntry<Named, Task> be1 = beanEntry(TaskWithDescriptor.class);
    final BeanEntry<Named, Task> be2 = beanEntry(TaskWithoutDescriptor.class);

    final DefaultNexusTaskFactory nexusTaskFactory = new DefaultNexusTaskFactory(
        ImmutableList.of(be1, be2));

    assertThat(nexusTaskFactory.isTask(TaskWithDescriptor.class.getName()), is(true));
    assertThat(nexusTaskFactory.isTask(TaskWithoutDescriptor.class.getName()), is(true));
    assertThat(nexusTaskFactory.isTask(String.class.getName()), is(false));
    assertThat(nexusTaskFactory.isTask("foobar"), is(false));
  }

  @Test
  public void createTaskInstance() {
    final BeanEntry<Named, Task> be1 = beanEntry(TaskWithDescriptor.class);
    final BeanEntry<Named, Task> be2 = beanEntry(TaskWithoutDescriptor.class);

    final DefaultNexusTaskFactory nexusTaskFactory = new DefaultNexusTaskFactory(
        ImmutableList.of(be1, be2));

    final TaskConfiguration c1 = new TaskConfiguration();
    c1.setId("id");
    c1.setType(TaskWithDescriptor.class.getName());
    final Task<?> task1 = nexusTaskFactory.createTaskInstance(c1);
    assertThat(task1, is(instanceOf(TaskWithDescriptor.class)));

    final TaskConfiguration c2 = new TaskConfiguration();
    c2.setId("id");
    c2.setType(TaskWithoutDescriptor.class.getName());
    final Task<?> task2 = nexusTaskFactory.createTaskInstance(c2);
    assertThat(task2, is(instanceOf(TaskWithoutDescriptor.class)));

    final TaskConfiguration c3 = new TaskConfiguration();
    c3.setId("id");
    c2.setType("foobar");
    try {
      final Task<?> task3 = nexusTaskFactory.createTaskInstance(c2);
      fail("This should not return");
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), containsString("foobar"));
    }
  }
}

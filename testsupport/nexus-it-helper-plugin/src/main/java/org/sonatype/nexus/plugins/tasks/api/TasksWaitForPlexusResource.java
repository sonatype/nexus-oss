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
package org.sonatype.nexus.plugins.tasks.api;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class TasksWaitForPlexusResource
    extends AbstractPlexusResource
{
  private static final Logger log = LoggerFactory.getLogger(TasksWaitForPlexusResource.class);

  private static final String RESOURCE_URI = "/tasks/waitFor";

  private final TaskScheduler nexusScheduler;

  @Inject
  public TasksWaitForPlexusResource(final TaskScheduler nexusScheduler)
  {
    this.nexusScheduler = checkNotNull(nexusScheduler);
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "anon");
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    final Form form = request.getResourceRef().getQueryAsForm();
    final String name = form.getFirstValue("name");
    final String taskType = form.getFirstValue("taskType");
    final long window = Long.parseLong(form.getFirstValue("window", "10000"));
    final long timeout = Long.parseLong(form.getFirstValue("timeout", "60000"));

    final TaskInfo<?> namedTask = getTaskByName(nexusScheduler, name);

    if (name != null && namedTask == null) {
      // task wasn't found, so bounce on outta here
      response.setStatus(Status.SUCCESS_OK);
      return "OK";
    }

    long lastTimeTasksWereStillRunning = System.currentTimeMillis();
    final long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime <= timeout) {
      sleep();

      if (isTaskCompleted(nexusScheduler, taskType, namedTask)) {
        if (System.currentTimeMillis() - lastTimeTasksWereStillRunning >= window) {
          response.setStatus(Status.SUCCESS_OK);
          return "OK";
        }
      }
      else {
        lastTimeTasksWereStillRunning = System.currentTimeMillis();
      }
    }

    response.setStatus(Status.SUCCESS_ACCEPTED);
    return "Tasks Not Finished";
  }

  static boolean isTaskCompleted(final TaskScheduler nexusScheduler,
                                 final String taskType,
                                 final TaskInfo<?> namedTask)
  {
    if (namedTask != null) {
      return isTaskCompleted(namedTask);
    }
    else {
      for (final TaskInfo<?> task : getTasks(nexusScheduler, taskType)) {
        if (!isTaskCompleted(task)) {
          return false;
        }
      }
      return true;
    }
  }

  static TaskInfo<?> getTaskByName(final TaskScheduler nexusScheduler, final String name) {
    if (name == null) {
      return null;
    }

    final List<TaskInfo<?>> tasks = nexusScheduler.listsTasks();
    for (TaskInfo<?> task : tasks) {
      if (task.getName().equals(name)) {
        return task;
      }
    }

    return null;
  }

  static void sleep() {
    try {
      Thread.sleep(500);
    }
    catch (final InterruptedException e) {
      // ignore
    }
  }

  private static boolean isTaskCompleted(TaskInfo<?> task) {
    log.debug("task: {} : {}, currentState: {}, endState: {}", task.getId(), task.getName(),
        task.getCurrentState().getState(),
        task.getLastRunState() != null ? task.getLastRunState().getEndState() : "n/a");
    return State.RUNNING != task.getCurrentState().getState() && task.getLastRunState() != null;
  }

  private static List<TaskInfo<?>> getTasks(final TaskScheduler nexusScheduler, final String taskType) {
    final List<TaskInfo<?>> tasks = nexusScheduler.listsTasks();
    return Lists.newArrayList(Iterables.filter(tasks, new Predicate<TaskInfo<?>>()
    {
      @Override
      public boolean apply(final TaskInfo<?> input) {
        return taskType == null || taskType.equals(input.getConfiguration().getTypeId());
      }
    }));
  }

}

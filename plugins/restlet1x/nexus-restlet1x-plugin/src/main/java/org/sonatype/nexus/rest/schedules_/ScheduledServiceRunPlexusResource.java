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
package org.sonatype.nexus.rest.schedules_;

import java.util.Date;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState;
import org.sonatype.nexus.scheduling.TaskInfo.LastRunState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.TaskRemovedException;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * @author tstevens
 */
@Named
@Singleton
@Path(ScheduledServiceRunPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
public class ScheduledServiceRunPlexusResource
    extends AbstractScheduledServicePlexusResource
{
  public static final String RESOURCE_URI = "/schedule_run/{" + SCHEDULED_SERVICE_ID_KEY + "}";

  public ScheduledServiceRunPlexusResource() {
    setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/schedule_run/*", "authcBasic,perms[nexus:tasksrun]");
  }

  /**
   * Run the specified scheduled task right now. Will then be rescheduled upon completion for normal run.
   *
   * @param scheduledServiceId The scheduled task to access.
   */
  @Override
  @GET
  public ScheduledServiceResourceStatusResponse get(Context context, Request request, Response response,
                                                    Variant variant)
      throws ResourceException
  {
    ScheduledServiceResourceStatusResponse result = null;

    final String scheduledServiceId = getScheduledServiceId(request);

    TaskInfo<?> task = getNexusScheduler().getTaskById(scheduledServiceId);
    if (task == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
          + scheduledServiceId);
    }

    try {
      task.runNow();
      final TaskConfiguration taskConfiguration = task.getConfiguration();
      final Schedule schedule = task.getSchedule();
      final CurrentState currentState = task.getCurrentState();
      final LastRunState lastRunState = task.getLastRunState();

      ScheduledServiceBaseResource resource = getServiceRestModel(taskConfiguration, schedule);

      if (resource != null) {
        ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
        resourceStatus.setResource(resource);
        resourceStatus.setResourceURI(createChildReference(request, this, taskConfiguration.getId()).toString());
        resourceStatus.setStatus(currentState.getState().name());
        resourceStatus.setReadableStatus(getReadableState(task));
        resourceStatus.setCreated(taskConfiguration.getCreated().toString());
        resourceStatus.setLastRunResult(getLastRunResult(task));
        resourceStatus.setLastRunTime(lastRunState == null ? "n/a" : lastRunState.getRunStarted().toString());
        final Date nextRunTime = getNextRunTime(task);
        resourceStatus.setNextRunTime(nextRunTime == null ? "n/a" : nextRunTime.toString());
        resourceStatus.setCreatedInMillis(taskConfiguration.getCreated().getTime());
        if (lastRunState != null) {
          resourceStatus.setLastRunTimeInMillis(task.getLastRunState().getRunStarted().getTime());
        }
        if (currentState.getNextRun() != null) {
          resourceStatus.setNextRunTimeInMillis(task.getCurrentState().getNextRun().getTime());
        }

        result = new ScheduledServiceResourceStatusResponse();
        result.setData(resourceStatus);
      }
      else {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Invalid schedule id ("
            + scheduledServiceId + "), can't load task.");
      }
      return result;

    }
    catch (TaskRemovedException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
          + scheduledServiceId);
    }
    catch (IllegalStateException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
          + scheduledServiceId);
    }
  }

  /**
   * Cancel the execution of an existing scheduled task.
   *
   * @param scheduledServiceId The scheduled task to cancel.
   */
  @Override
  @DELETE
  public void delete(Context context, Request request, Response response)
      throws ResourceException
  {
    final TaskInfo<?> task = getNexusScheduler().getTaskById(getScheduledServiceId(request));
    if (task != null) {
      if (State.RUNNING == task.getCurrentState().getState()) {
        task.getCurrentState().getFuture().cancel(false);
      }
      response.setStatus(Status.SUCCESS_NO_CONTENT);
      return;
    }
    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Resource not found");
  }

  // ==

  protected String getScheduledServiceId(Request request) {
    return request.getAttributes().get(SCHEDULED_SERVICE_ID_KEY).toString();
  }
}

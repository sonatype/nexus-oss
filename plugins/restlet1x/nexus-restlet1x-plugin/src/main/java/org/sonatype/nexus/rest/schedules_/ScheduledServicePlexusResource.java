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
package org.sonatype.nexus.rest.schedules_;

import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.State;
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
@Path(ScheduledServicePlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class ScheduledServicePlexusResource
    extends AbstractScheduledServicePlexusResource
{
  public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

  public static final String RESOURCE_URI = "/schedules/{" + SCHEDULED_SERVICE_ID_KEY + "}";

  public ScheduledServicePlexusResource() {
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new ScheduledServiceResourceResponse();
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/schedules/*", "authcBasic,perms[nexus:tasks]");
  }

  protected String getScheduledServiceId(Request request) {
    return request.getAttributes().get(SCHEDULED_SERVICE_ID_KEY).toString();
  }

  /**
   * Get the details of an existing scheduled task.
   *
   * @param scheduledServiceId The scheduled task to access.
   */
  @Override
  @GET
  public ScheduledServiceResourceResponse get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    ScheduledServiceResourceResponse result = new ScheduledServiceResourceResponse();
    TaskInfo<?> task = getNexusScheduler().getTaskById(getScheduledServiceId(request));
    if (task == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
          + getScheduledServiceId(request));
    }

    try {
      ScheduledServiceBaseResource resource = getServiceRestModel(task.getConfiguration(), task.getSchedule());
      if (resource != null) {
        result.setData(resource);
      }
      else {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Invalid schedule id ("
            + getScheduledServiceId(request) + "), can't load task.");
      }

      return result;
    }
    catch (IllegalStateException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
          + getScheduledServiceId(request));
    }
  }

  /**
   * Update the configuration of an existing scheduled task.
   *
   * @param scheduledServiceId The scheduled task to access.
   */
  @Override
  @PUT
  public ScheduledServiceResourceStatusResponse put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    ScheduledServiceResourceResponse serviceRequest = (ScheduledServiceResourceResponse) payload;
    ScheduledServiceResourceStatusResponse result = null;

    if (serviceRequest != null) {
      ScheduledServiceBaseResource resource = serviceRequest.getData();

      try {
        // currently we allow editing of:
        // task name
        // task schedule (even to another type)
        // task params
        TaskInfo<?> task = getNexusScheduler().getTaskById(getScheduledServiceId(request));
        if (task == null) {
          getLogger().warn("Unable to locate task id:" + resource.getId());
          throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!");
        }
        if (task.getCurrentState().getState() == State.RUNNING) {
          throw new ResourceException(Status.CLIENT_ERROR_CONFLICT,
              "Task can't be edited while it is being executed or it is in line to be executed");
        }

        task.getConfiguration().setEnabled(resource.isEnabled());

        task.getConfiguration().setName(getModelName(resource));

        Schedule schedule = getModelSchedule(resource);

        for (Iterator<ScheduledServicePropertyResource> iter = resource.getProperties().iterator(); iter.hasNext(); ) {
          ScheduledServicePropertyResource prop = iter.next();
          task.getConfiguration().getMap().put(prop.getKey(), prop.getValue());
        }

        task.getConfiguration().setAlertEmail(resource.getAlertEmail());
        if (resource.getName() != null) {
          task.getConfiguration().setName(resource.getName());
        }

        // Store the changes
        task = getNexusScheduler().scheduleTask(task.getConfiguration(), schedule);

        ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
        resourceStatus.setResource(resource);
        // Just need to update the id, as the incoming data is a POST w/ no id
        resourceStatus.getResource().setId(task.getId());
        resourceStatus.setResourceURI(createChildReference(request, this, task.getId()).toString());
        resourceStatus.setStatus(task.getCurrentState().getState().name());
        resourceStatus.setReadableStatus(getReadableState(task));
        resourceStatus.setCreated(task.getConfiguration().getCreated().toString());
        resourceStatus.setLastRunResult(getLastRunResult(task));
        resourceStatus.setLastRunTime(task.getLastRunState() == null ? "n/a" : task.getLastRunState().getRunStarted().toString());
        final Date nextRunTime = getNextRunTime(task);
        resourceStatus.setNextRunTime(nextRunTime == null ? "n/a" : nextRunTime.toString());
        resourceStatus.setCreatedInMillis(task.getConfiguration().getUpdated().getTime());
        if (task.getLastRunState() != null) {
          resourceStatus.setLastRunTimeInMillis(task.getLastRunState().getRunStarted().getTime());
        }
        if (nextRunTime != null) {
          resourceStatus.setNextRunTimeInMillis(nextRunTime.getTime());
        }

        result = new ScheduledServiceResourceStatusResponse();
        result.setData(resourceStatus);

      }
      catch (RejectedExecutionException e) {
        throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, e.getMessage());
      }
      catch (ParseException e) {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
      }
      catch (InvalidConfigurationException e) {
        handleConfigurationException(e);
      }
    }
    return result;
  }

  /**
   * Delete an existing scheduled task.
   *
   * @param scheduledServiceId The scheduled task to access.
   */
  @Override
  @DELETE
  public void delete(Context context, Request request, Response response)
      throws ResourceException
  {
    final TaskInfo<?> task = getNexusScheduler().getTaskById(getScheduledServiceId(request));
    if (task != null) {
      task.remove();
      response.setStatus(Status.SUCCESS_NO_CONTENT);
      return;
    }
    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Resource not found");
  }

}

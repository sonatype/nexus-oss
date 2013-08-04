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

package org.sonatype.nexus.rest.status;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

@Component(role = ManagedPlexusResource.class, hint = "CommandPlexusResource")
@Path(CommandPlexusResource.RESOURCE_URI)
@Consumes({"application/xml", "application/json"})
public class CommandPlexusResource
    extends AbstractNexusPlexusResource
    implements ManagedPlexusResource
{
  public static final String RESOURCE_URI = "/status/command";

  public CommandPlexusResource() {
    this.setReadable(false);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new String();
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:command]");
  }

  /**
   * Control the nexus server, you can START, STOP, RESTART or KILL it.
   */
  @Override
  @PUT
  @ResourceMethodSignature(input = String.class)
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    boolean result = false;

    try {
      String cmd = (String) payload;

      if ("START".equalsIgnoreCase(cmd)) {
        result = getNexus().setState(SystemState.STARTED);
      }
      else if ("STOP".equalsIgnoreCase(cmd)) {
        result = getNexus().setState(SystemState.STOPPED);
      }
      else if ("RESTART".equalsIgnoreCase(cmd)) {
        // if running stop it
        if (SystemState.STARTED.equals(getNexus().getSystemStatus().getState())) {
          getNexus().setState(SystemState.STOPPED);
        }

        // and start it
        result = getNexus().setState(SystemState.STARTED);
      }
      else if ("KILL".equalsIgnoreCase(cmd)) {
        // if running stop it
        if (SystemState.STARTED.equals(getNexus().getSystemStatus().getState())) {
          getNexus().setState(SystemState.STOPPED);
        }

        System.exit(0);
      }
      else {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown COMMAND!");
      }

      if (result) {
        response.setStatus(Status.SUCCESS_NO_CONTENT);
      }
      else {
        throw new ResourceException(
            Status.CLIENT_ERROR_BAD_REQUEST,
            "Could not change Nexus state to submitted one! (check logs for more info)");
      }
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(
          Status.CLIENT_ERROR_BAD_REQUEST,
          "Could not change Nexus state to submitted one! (check logs for more info)");
    }
    // status is 204
    return null;
  }

}

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

package org.sonatype.nexus.rest.component;

import java.util.Map;

import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public abstract class AbstractComponentListPlexusResource
    extends AbstractNexusPlexusResource
{
  public static final String ROLE_ID = "role";

  @Requirement
  private PlexusContainer container;

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  protected String getRole(Request request) {
    return request.getAttributes().get(ROLE_ID).toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    PlexusComponentListResourceResponse result = new PlexusComponentListResourceResponse();

    // get role from request
    String role = getRole(request);

    try {
      Map<String, Object> components = container.lookupMap(role);

      if (components == null || components.isEmpty()) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }

      for (String hint : components.keySet()) {
        ComponentDescriptor componentDescriptor = container.getComponentDescriptor(role, hint);

        PlexusComponentListResource resource = new PlexusComponentListResource();

        resource.setRoleHint(componentDescriptor.getRoleHint());
        resource.setDescription((StringUtils.isNotEmpty(componentDescriptor.getDescription()))
            ? componentDescriptor.getDescription()
            : componentDescriptor.getRoleHint());

        // add it to the collection
        result.addData(resource);
      }

    }
    catch (ComponentLookupException e) {
      if (this.getLogger().isDebugEnabled()) {
        getLogger().debug("Unable to look up plexus component with role '" + "1" + "'.", e);
      }

      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
    }

    return result;
  }
}

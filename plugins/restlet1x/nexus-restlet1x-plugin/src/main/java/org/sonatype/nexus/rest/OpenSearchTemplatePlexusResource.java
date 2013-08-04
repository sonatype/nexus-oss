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

package org.sonatype.nexus.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.plexus.rest.representation.VelocityRepresentation;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

@Component(role = PlexusResource.class, hint = "openSearchTemplate")
@Path("/opensearch")
@Produces("text/xml")
public class OpenSearchTemplatePlexusResource
    extends AbstractNexusPlexusResource
{

  public OpenSearchTemplatePlexusResource() {
    super();
    setReadable(true);
    setModifiable(false);
  }

  @Override
  public Object getPayloadInstance() {
    // RO resource
    return null;
  }

  @Override
  public String getResourceUri() {
    return "/opensearch";
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    // the client should have index access for the search to work
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:index]");
  }

  /**
   * Provides the OpenSearch description document for this Nexus instance. For the emitted XML, see <a
   * href="http://www.opensearch.org/Specifications/OpenSearch/1.1#OpenSearch_description_document">OpenSearch
   * Description Document</a>.
   */
  @Override
  @GET
  @ResourceMethodSignature(output = String.class)
  public Representation get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    Map<String, Object> map = new HashMap<String, Object>();

    Reference nexusRef = getContextRoot(request);
    String nexusRoot = nexusRef.toString();
    if (nexusRoot.endsWith("/")) {
      nexusRoot = nexusRoot.substring(0, nexusRoot.length() - 1);
    }

    map.put("nexusRoot", nexusRoot);
    map.put("nexusHost", nexusRef.getHostDomain());

    final VelocityRepresentation templateRepresentation =
        new VelocityRepresentation(context, "/templates/opensearch.vm", getClass().getClassLoader(), map,
            MediaType.TEXT_XML);

    return templateRepresentation;
  }
}

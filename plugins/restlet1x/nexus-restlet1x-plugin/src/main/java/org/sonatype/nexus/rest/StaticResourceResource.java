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

import java.util.Collections;

import org.sonatype.nexus.plugins.rest.StaticResource;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

public class StaticResourceResource
    extends Resource
{
  private final StaticResource resource;

  public StaticResourceResource(Context ctx, Request req, Response rsp, StaticResource resource) {
    super(ctx, req, rsp);

    setVariants(Collections.singletonList(new Variant(MediaType.valueOf(resource.getContentType()))));

    this.resource = resource;
  }

  public Representation represent(Variant variant)
      throws ResourceException
  {
    return new StaticResourceRepresentation(resource);
  }

}

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

package org.sonatype.nexus.plugins.ui.rest;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource to redirect to the absolute URI to the index.html.
 */
@Named("IndexRedirectingPlexusResource")
@Singleton
@Typed(ManagedPlexusResource.class)
public class IndexRedirectingPlexusResource
    extends AbstractNexusPlexusResource
    implements ManagedPlexusResource
{

  private final ManagedPlexusResource indexTemplateResource;

  @Inject
  public IndexRedirectingPlexusResource(final IndexTemplatePlexusResource indexTemplateResource) {
    this.indexTemplateResource = indexTemplateResource;
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return null;
  }

  @Override
  public String getResourceUri() {
    return "";
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    response.redirectPermanent(
        createRootReference(
            request, indexTemplateResource.getResourceUri().replaceFirst("/", "")
        )
    );

    return null;
  }
}

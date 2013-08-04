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

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

@Component(role = ManagedPlexusResource.class, hint = "content")
public class ContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
    implements ManagedPlexusResource
{
  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public String getResourceUri() {
    // this is managed plexus resource, so path is not important
    return "";
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/**", "contentAuthcBasic,contentTperms");
  }

  public List<Variant> getVariants() {
    // TODO: A repository can contain any type of file, so this method needs to be more generic
    // it is likely that a site repository, would contain javascript, flash, etc
    // a maven repo would contain jars application/java-archive, etc
    List<Variant> result = super.getVariants();

    // default this presentation to HTML to enable user browsing
    result.add(0, new Variant(MediaType.TEXT_HTML));

    // css and javascript can be stored inside of a repository
    result.add(new Variant(MediaType.TEXT_CSS));
    result.add(new Variant(MediaType.TEXT_JAVASCRIPT));
    result.add(new Variant(MediaType.APPLICATION_JAVASCRIPT));

    // also support plain text content inside Nexus repositories
    result.add(new Variant(MediaType.TEXT_PLAIN));

    return result;
  }

  @Override
  protected ResourceStore getResourceStore(final Request request)
      throws NoSuchRepositoryException,
             ResourceException
  {
    return getNexus().getRootRouter();
  }
}

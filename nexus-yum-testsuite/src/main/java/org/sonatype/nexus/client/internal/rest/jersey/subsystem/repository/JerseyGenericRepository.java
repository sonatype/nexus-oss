/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.repository;

import org.sonatype.nexus.client.core.subsystem.repository.GenericRepository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;

public class JerseyGenericRepository
    extends JerseyRepositorySupport<GenericRepository, RepositoryResource>
    implements GenericRepository
{

    public JerseyGenericRepository( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    protected RepositoryResource createSettings()
    {
        return new RepositoryResource();
    }

  @Override
  protected Class<? extends NexusResponse> getResponseClass() {
    return RepositoryResourceResponse.class;
  }

  @Override
  protected RepositoryResource getData(Object response) {
    return (RepositoryResource) ((RepositoryResourceResponse) response).getData();
  }

}

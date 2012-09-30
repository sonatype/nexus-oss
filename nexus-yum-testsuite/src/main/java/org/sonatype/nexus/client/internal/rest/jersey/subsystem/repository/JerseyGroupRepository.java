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

import org.sonatype.nexus.client.core.subsystem.repository.GroupRepository;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;

public class JerseyGroupRepository
    extends JerseyRepositorySupport<GroupRepository, RepositoryGroupResource>
    implements GroupRepository
{

    public JerseyGroupRepository( final JerseyNexusClient nexusClient )
    {
        super( nexusClient );
    }

    @Override
    protected RepositoryGroupResource createSettings()
    {
        final RepositoryGroupResource settings = new RepositoryGroupResource();

        settings.setExposed( true );

        return settings;
    }

    @Override
    String uri()
    {
        return "repo_groups";
    }

  @Override
  protected Class<? extends NexusResponse> getResponseClass() {
    return RepositoryGroupResourceResponse.class;
  }

  @Override
  protected RepositoryGroupResource getData(Object response) {
    return ((RepositoryGroupResourceResponse) response).getData();
  }
}

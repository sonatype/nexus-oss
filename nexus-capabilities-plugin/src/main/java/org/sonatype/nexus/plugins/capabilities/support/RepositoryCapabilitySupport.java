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
package org.sonatype.nexus.plugins.capabilities.support;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class RepositoryCapabilitySupport
    extends CapabilitySupport
{

    private final RepositoryRegistry repositoryRegistry;

    public RepositoryCapabilitySupport( final CapabilityContext context,
                                        final RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = checkNotNull( repositoryRegistry, "Repository registry cannot be null" );
    }

    protected Repository getRepository( final String repositoryId,
                                        final Map<String, String> properties )
    {
        String property = properties.get( checkNotNull( repositoryId, "Repository id cannot be null" ) );
        try
        {
            property = property.replaceFirst( "repo_", "" );
            property = property.replaceFirst( "group_", "" );
            return repositoryRegistry.getRepository( property );
        }
        catch ( final NoSuchRepositoryException e )
        {
            throw new RuntimeException( String.format( "Cannot find repository with id '%s'", property ) );
        }
    }
}

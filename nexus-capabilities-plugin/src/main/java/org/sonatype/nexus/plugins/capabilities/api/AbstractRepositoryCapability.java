/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class AbstractRepositoryCapability
    extends AbstractCapability
{

    private final RepositoryRegistry repositoryRegistry;

    public AbstractRepositoryCapability( final CapabilityIdentity id,
                                         final RepositoryRegistry repositoryRegistry )
    {
        super( id );
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

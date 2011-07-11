/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregatorConfiguration;

public class P2RepositoryAggregatorCapability
    extends AbstractCapability
{

    public static final String ID = "p2RepositoryAggregatorCapability";

    private final P2RepositoryAggregator service;

    private P2RepositoryAggregatorConfiguration configuration;

    public P2RepositoryAggregatorCapability( final String id, final P2RepositoryAggregator service )
    {
        super( id );
        this.service = service;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = new P2RepositoryAggregatorConfiguration( properties );
        service.addConfiguration( configuration );

        super.create( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        configuration = new P2RepositoryAggregatorConfiguration( properties );
        service.addConfiguration( configuration );

        super.load( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final P2RepositoryAggregatorConfiguration newConfiguration =
            new P2RepositoryAggregatorConfiguration( properties );
        if ( !configuration.equals( newConfiguration ) )
        {
            remove();
            create( properties );
        }

        super.update( properties );
    }

    @Override
    public void remove()
    {
        service.removeConfiguration( configuration );

        super.remove();
    }

}

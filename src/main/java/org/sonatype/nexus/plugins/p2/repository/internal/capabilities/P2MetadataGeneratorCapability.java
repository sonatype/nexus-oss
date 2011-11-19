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
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;

public class P2MetadataGeneratorCapability
    extends AbstractCapability
{

    public static final String ID = "p2MetadataCapability";

    private final P2MetadataGenerator service;

    private P2MetadataGeneratorConfiguration configuration;

    public P2MetadataGeneratorCapability( final String id, final P2MetadataGenerator service )
    {
        super( id );
        this.service = service;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = createConfiguration( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        create( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final P2MetadataGeneratorConfiguration newConfiguration = createConfiguration( properties );
        if ( !configuration.equals( newConfiguration ) )
        {
            passivate();
            configuration = newConfiguration;
            activate();
        }
    }

    @Override
    public void activate()
    {
        service.addConfiguration( configuration );
    }

    @Override
    public void passivate()
    {
        service.removeConfiguration( configuration );
    }

    private P2MetadataGeneratorConfiguration createConfiguration( final Map<String, String> properties )
    {
        return new P2MetadataGeneratorConfiguration( properties );
    }

}

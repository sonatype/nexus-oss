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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.p2.repository.internal.capabilities.P2RepositoryAggregatorCapabilityDescriptor.TYPE_ID;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.support.condition.Conditions;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregatorConfiguration;

@Named( TYPE_ID )
public class P2RepositoryAggregatorCapability
    extends CapabilitySupport
{

    private final P2RepositoryAggregator service;

    private final Conditions conditions;

    private P2RepositoryAggregatorConfiguration configuration;

    @Inject
    public P2RepositoryAggregatorCapability( final P2RepositoryAggregator service,
                                             final Conditions conditions )
    {
        this.service = checkNotNull( service );
        this.conditions = checkNotNull( conditions );
    }

    @Override
    public void onActivate()
    {
        configuration = createConfiguration( context().properties() );
        service.addConfiguration( configuration );
    }

    @Override
    public void onPassivate()
    {
        service.removeConfiguration( configuration );
    }

    @Override
    public Condition activationCondition()
    {
        return conditions.capabilities().passivateCapabilityDuringUpdate( context().id() );
    }

    private P2RepositoryAggregatorConfiguration createConfiguration( final Map<String, String> properties )
    {
        return new P2RepositoryAggregatorConfiguration( properties );
    }

}

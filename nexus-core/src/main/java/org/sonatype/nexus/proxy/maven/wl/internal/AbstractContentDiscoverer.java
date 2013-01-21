/*
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
package org.sonatype.nexus.proxy.maven.wl.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.Prioritized.PriorityOrderingComparator;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.Strategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;

/**
 * Common grounds for {@link LocalContentDiscoverer} and {@link RemoteContentDiscoverer} implementations.
 * 
 * @author cstamas
 * @since 2.4
 * @param <R>
 * @param <S>
 */
public abstract class AbstractContentDiscoverer<R extends MavenRepository, S extends Strategy>
{

    protected DiscoveryResult discoverContent( final List<S> strategies, final R mavenRepository )
        throws IOException
    {
        final ArrayList<S> appliedStrategies = new ArrayList<S>( strategies );
        Collections.sort( appliedStrategies, new PriorityOrderingComparator<S>() );
        final DiscoveryResult discoveryResult = new DiscoveryResult( mavenRepository );
        for ( S strategy : appliedStrategies )
        {
            discoverContentWithStrategy( strategy, mavenRepository, discoveryResult );
            if ( discoveryResult.isSuccessful() )
            {
                break;
            }
        }
        return discoveryResult;
    }

    protected void discoverContentWithStrategy( final S strategy, final R mavenRepository,
                                                final DiscoveryResult discoveryResult )
        throws IOException
    {
        try
        {
            final EntrySource entrySource = discover( strategy, mavenRepository );
            discoveryResult.recordSuccess( strategy, entrySource );
        }
        catch ( StrategyFailedException e )
        {
            discoveryResult.recordFailure( strategy, e );
        }
    }

    protected abstract EntrySource discover( S strategy, R mavenRepository )
        throws StrategyFailedException, IOException;
}

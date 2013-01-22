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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.wl.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalContentDiscoverer;
import org.sonatype.nexus.proxy.maven.wl.discovery.LocalStrategy;

/**
 * Default {@link LocalContentDiscoverer} implementation.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class LocalContentDiscovererImpl
    extends AbstractContentDiscoverer<MavenHostedRepository, LocalStrategy>
    implements LocalContentDiscoverer
{
    private final List<LocalStrategy> localStrategies;

    /**
     * Constructor.
     * 
     * @param localStrategies
     */
    @Inject
    public LocalContentDiscovererImpl( final List<LocalStrategy> localStrategies )
    {
        this.localStrategies = checkNotNull( localStrategies );
    }

    @Override
    public DiscoveryResult<MavenHostedRepository> discoverLocalContent( final MavenHostedRepository mavenRepository )
        throws IOException
    {
        return discoverContent( localStrategies, mavenRepository );
    }
}

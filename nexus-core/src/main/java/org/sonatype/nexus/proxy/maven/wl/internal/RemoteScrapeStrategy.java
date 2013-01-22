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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.proxy.maven.wl.discovery.RemoteStrategy;
import org.sonatype.nexus.proxy.maven.wl.discovery.StrategyFailedException;

/**
 * Remote scrape strategy.
 * 
 * @author cstamas
 */
@Named( RemoteScrapeStrategy.ID )
@Singleton
public class RemoteScrapeStrategy
    extends AbstractStrategy<MavenProxyRepository>
    implements RemoteStrategy
{
    protected static final String ID = "scrape";

    private final WLConfig config;

    @Inject
    public RemoteScrapeStrategy( final WLConfig config )
    {
        super( ID, Integer.MAX_VALUE );
        this.config = checkNotNull( config );
    }

    @Override
    public EntrySource discover( final MavenProxyRepository mavenProxyRepository )
        throws StrategyFailedException, IOException
    {
        // scrape remote of passed in proxy repository up to config.getRemoteScrapeDepth() depth
        // fail
        throw new StrategyFailedException( "Not implemented!" );
    }
}

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
package org.sonatype.nexus.proxy.maven.wl.discovery;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;

/**
 * Class carrying the results of a discovery.
 * 
 * @author cstamas
 * @since 2.4
 * @param <R>
 */
public class DiscoveryResult<R extends MavenRepository>
{
    private final R mavenRepository;

    private final Map<String, Throwable> failures;

    private boolean successful;

    private Strategy<R> strategy;

    private EntrySource entrySource;

    /**
     * Constructor.
     * 
     * @param mavenRepository the repository having been discovered.
     */
    public DiscoveryResult( final R mavenRepository )
    {
        this.mavenRepository = checkNotNull( mavenRepository );
        this.failures = new HashMap<String, Throwable>();
        this.successful = false;
        this.strategy = null;
        this.entrySource = null;
    }

    /**
     * Returns the repository being discovered.
     * 
     * @return the discovered repository.
     */
    public R getMavenRepository()
    {
        return mavenRepository;
    }

    /**
     * Failures happened during discovery reported by failed strategies. Map is keyed with {@link Strategy#getId()} and
     * values are the {@link Throwable} instances reported by them.
     * 
     * @return map with failures.
     */
    public Map<String, Throwable> getFailures()
    {
        return failures;
    }

    /**
     * Returns {@code true} if discovery was successful.
     * 
     * @return {@code true} if discovery was successful.
     */
    public boolean isSuccessful()
    {
        return successful;
    }

    /**
     * Returns the succeeded strategy instance.F
     * 
     * @return strategy that succeeded.
     */
    public Strategy<R> getStrategy()
    {
        return strategy;
    }

    /**
     * Returns the {@link EntrySource} that was provided by successful strategy.
     * 
     * @return entry source built by successful strategy.
     */
    public EntrySource getEntrySource()
    {
        return entrySource;
    }

    /**
     * Records a success on behalf of a strategy.
     * 
     * @param usedStrategy
     * @param entrySource
     */
    public void recordSuccess( final Strategy<R> usedStrategy, final EntrySource entrySource )
    {
        this.successful = true;
        this.strategy = checkNotNull( usedStrategy );
        this.entrySource = checkNotNull( entrySource );
    }

    /**
     * Records a failure on behalf of a strategy.
     * 
     * @param usedStrategy
     * @param failureCause
     */
    public void recordFailure( final Strategy<R> usedStrategy, final Throwable failureCause )
    {
        this.failures.put( usedStrategy.getId(), failureCause );
    }
}

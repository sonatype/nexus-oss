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

import java.util.ArrayList;
import java.util.List;

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
    public static abstract class Outcome
    {
        private final String strategyId;

        private final boolean successful;

        private final String message;

        public Outcome( final String strategyId, final boolean successful, final String message )
        {
            this.strategyId = checkNotNull( strategyId );
            this.successful = successful;
            this.message = checkNotNull( message );
        }

        public String getStrategyId()
        {
            return strategyId;
        }

        public boolean isSuccessful()
        {
            return successful;
        }

        public String getMessage()
        {
            return message;
        }
    }

    public static class SuccessfulOutcome
        extends Outcome
    {
        public SuccessfulOutcome( final String strategyId, final String message )
        {
            super( strategyId, true, message );
        }
    }

    public static class FailedOutcome
        extends Outcome
    {
        private final Throwable throwable;

        public FailedOutcome( final String strategyId, final Throwable throwable )
        {
            super( strategyId, false, throwable.getMessage() );
            this.throwable = throwable;
        }

        public Throwable getThrowable()
        {
            return throwable;
        }
    }

    private final R mavenRepository;

    private final List<Outcome> outcomes;

    private EntrySource entrySource;

    /**
     * Constructor.
     * 
     * @param mavenRepository the repository having been discovered.
     */
    public DiscoveryResult( final R mavenRepository )
    {
        this.mavenRepository = checkNotNull( mavenRepository );
        this.outcomes = new ArrayList<Outcome>();
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
     * Returns {@code true} if discovery was successful.
     * 
     * @return {@code true} if discovery was successful.
     */
    public boolean isSuccessful()
    {
        return getLastSuccessOutcome() != null;
    }

    /**
     * Returns the succeeded strategy instance.F
     * 
     * @return strategy that succeeded.
     */
    public Outcome getLastResult()
    {
        return getLastOutcome();
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
     * @param usedStrategyId
     * @param message
     * @param entrySource
     */
    public void recordSuccess( final String usedStrategyId, final String message, final EntrySource entrySource )
    {
        checkNotNull( usedStrategyId );
        checkNotNull( message );
        checkNotNull( entrySource );
        if ( !isSuccessful() )
        {
            final SuccessfulOutcome success = new SuccessfulOutcome( usedStrategyId, message );
            this.outcomes.add( success );
            this.entrySource = entrySource;
        }
    }

    /**
     * Records a failure on behalf of a strategy.
     * 
     * @param usedStrategyId
     * @param failureCause
     */
    public void recordFailure( final String usedStrategyId, final Throwable failureCause )
    {
        checkNotNull( usedStrategyId );
        checkNotNull( failureCause );
        if ( !isSuccessful() )
        {
            final FailedOutcome failure = new FailedOutcome( usedStrategyId, failureCause );
            this.outcomes.add( failure );
        }
    }

    // ==

    protected Outcome getLastOutcome()
    {
        if ( outcomes.size() > 0 )
        {
            final Outcome outcome = outcomes.get( outcomes.size() - 1 );
            return outcome;
        }
        return null;
    }

    protected SuccessfulOutcome getLastSuccessOutcome()
    {
        final Outcome outcome = getLastOutcome();
        if ( outcome instanceof SuccessfulOutcome )
        {
            return (SuccessfulOutcome) outcome;
        }
        return null;
    }
}

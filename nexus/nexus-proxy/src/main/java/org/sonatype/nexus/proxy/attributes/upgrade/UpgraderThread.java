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
package org.sonatype.nexus.proxy.attributes.upgrade;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RecreateAttributesWalker;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.proxy.walker.WalkerThrottleController;
import org.sonatype.nexus.util.FibonacciNumberSequence;

public class UpgraderThread
    extends Thread
    implements WalkerThrottleController
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final File legacyAttributesDirectory;

    private final RepositoryRegistry repositoryRegistry;

    private int actualUps;

    private int limiterUps;

    private FibonacciNumberSequence currentSleepTime;

    private long lastAdjustment;

    public UpgraderThread( final File legacyAttributesDirectory, final RepositoryRegistry repositoryRegistry,
                           final int limiterUps )
    {
        this.legacyAttributesDirectory = legacyAttributesDirectory;
        this.repositoryRegistry = repositoryRegistry;
        this.limiterUps = limiterUps;
        // start with sleep time of 100ms
        this.currentSleepTime = new FibonacciNumberSequence( 100 );
        this.lastAdjustment = 0;
        // to have it clearly in thread dumps
        setName( "LegacyAttributesUpgrader" );
        // to not prevent sudden reboots (by user, if upgrading, and rebooting)
        setDaemon( true );
        // to not interfere much with other stuff (CPU wise)
        setPriority( Thread.MIN_PRIORITY );
    }

    public int getActualUps()
    {
        return actualUps;
    }

    public int getLimiterUps()
    {
        return limiterUps;
    }

    public void setLimiterUps( int limiterUps )
    {
        this.limiterUps = limiterUps;
    }

    @Override
    public void run()
    {
        // sleep a bit to not start prematurely (ie. nexus startup not done yet)
        try
        {
            TimeUnit.SECONDS.sleep( 5 );
        }
        catch ( InterruptedException e )
        {
            return;
        }
        // ping the "lastAdjustment" at very point where thread starts
        this.lastAdjustment = System.currentTimeMillis();

        if ( !DefaultAttributeUpgrader.isUpgradeDone( legacyAttributesDirectory, null ) )
        {
            List<Repository> reposes = repositoryRegistry.getRepositories();
            for ( Repository repo : reposes )
            {
                if ( !repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
                {
                    if ( !DefaultAttributeUpgrader.isUpgradeDone( legacyAttributesDirectory, repo.getId() ) )
                    {
                        logger.info( "Upgrading legacy attributes of repository {}.",
                            RepositoryStringUtils.getHumanizedNameString( repo ) );
                        ResourceStoreRequest req = new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT );
                        req.getRequestContext().put( WalkerThrottleController.CONTEXT_KEY, this );
                        req.getRequestContext().put( RecreateAttributesWalker.FORCE_ATTRIBUTE_RECREATION, Boolean.FALSE );
                        repo.recreateAttributes( req, null );
                        DefaultAttributeUpgrader.markUpgradeDone( legacyAttributesDirectory, repo.getId() );
                        logger.info( "Upgrade of legacy attributes of repository {} done.",
                            RepositoryStringUtils.getHumanizedNameString( repo ) );
                    }
                    else
                    {
                        logger.info( "Skipping legacy attributes of repository {}, already marked as upgraded.",
                            RepositoryStringUtils.getHumanizedNameString( repo ) );
                    }
                }
            }
            DefaultAttributeUpgrader.markUpgradeDone( legacyAttributesDirectory, null );
            logger.info(
                "Legacy attribute directory upgrade finished. Please delete, move or rename the \"{}\" folder.",
                legacyAttributesDirectory.getAbsolutePath() );
        }
    }

    // == WalkerThrottleController

    @Override
    public boolean isThrottled()
    {
        return limiterUps > 0;
    }

    @Override
    public long throttleTime( final ThrottleInfo info )
    {
        if ( adjustmentNeeded() )
        {
            actualUps = (int) ( info.getTotalProcessItemInvocationCount() / ( info.getTotalTimeWalking() / 1000 ) );

            if ( actualUps > limiterUps )
            {
                // hold down the horses, increase sleepTime
                if ( currentSleepTime.peek() <= 0 )
                {
                    currentSleepTime.reset();
                }
                else
                {
                    currentSleepTime.next();
                }
            }
            else
            {
                // lessen the sleep time
                if ( currentSleepTime.peek() > 0 )
                {
                    currentSleepTime.prev();
                }
            }

            logger.info( "Actual speed {} UPS (limited to {} UPS), current sleepTime {}ms.", new Object[] { actualUps,
                limiterUps, currentSleepTime.peek() } );
        }

        return currentSleepTime.peek();
    }

    /**
     * To prevent "oscillation" we do adjustments only once in 5 seconds (or override if needed).
     * 
     * @return
     */
    protected boolean adjustmentNeeded()
    {
        // adjust every 5 second for now
        if ( ( System.currentTimeMillis() - lastAdjustment ) > 5000 )
        {
            this.lastAdjustment = System.currentTimeMillis();

            return true;
        }
        else
        {
            return false;
        }
    }

}

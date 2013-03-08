package org.sonatype.nexus.testsuite.client;

import org.sonatype.nexus.testsuite.client.exception.WhitelistJobsAreStillRunningException;
import org.sonatype.sisu.goodies.common.Time;

/**
 * IT client for whitelist feature.
 * 
 * @author cstamas
 * @since 2.4
 */
public interface WhitelistTest
{
    /**
     * Blocks for one minute, or less, if white-list update jobs finished earlier.
     * 
     * @throws WhitelistJobsAreStillRunningException
     */
    void waitForAllWhitelistUpdateJobToStop()
        throws WhitelistJobsAreStillRunningException;

    /**
     * Blocks for given timeout, or less, if white-list update jobs finished earlier.
     * 
     * @param timeout
     * @throws WhitelistJobsAreStillRunningException
     */
    void waitForAllWhitelistUpdateJobToStop( Time timeout )
        throws WhitelistJobsAreStillRunningException;
}

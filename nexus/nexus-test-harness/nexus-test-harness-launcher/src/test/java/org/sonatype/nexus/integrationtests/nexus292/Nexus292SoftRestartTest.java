/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus292;

import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.NexusStateUtil;

/**
 * This test-harness uses the start/stop internally. This test will just poke at the state after the methods are called.
 */
public class Nexus292SoftRestartTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void softRebootTest()
        throws Exception
    {

        // make sure Nexus is running
        Assert.assertTrue( NexusStateUtil.isNexusRunning() );

        // restart
        NexusStateUtil.doSoftRestart();

        // make sure Nexus is running
        Assert.assertTrue( NexusStateUtil.isNexusRunning() );

        // now that we know the status comes back as STARTED, now we need to see if the rest of nexus will work at
        // all...
        Gav gav =
            new Gav( this.getTestId(), "soft-test", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // this will hurl if something bad happens
        this.downloadArtifact( gav, "target/downloads" );

        // TODO: we could tests other things like the feeds and other things, but for now this is good.
    }

    @Test
    public void softStopTest()
        throws Exception
    {

        // make sure Nexus is running
        Assert.assertTrue( NexusStateUtil.isNexusRunning() );

        // restart
        NexusStateUtil.doSoftStop();

        // make sure Nexus is not running
        Assert.assertFalse( NexusStateUtil.isNexusRunning() );

        // now that we know the status comes back as STARTED, now we need to see if the rest of nexus will work at
        // all...
        Gav gav =
            new Gav( this.getTestId(), "soft-test", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // this will hurl if something bad happens
        try
        {
            this.downloadArtifact( gav, "target/downloads" );
            Assert.fail( "IOException should have been thrown" );
        }
        catch ( IOException e )
        {
            // this is good
        }

        // restart
        NexusStateUtil.doSoftStart();

        // make sure Nexus is running
        Assert.assertTrue( NexusStateUtil.isNexusRunning() );

        // this will hurl if something bad happens
        this.downloadArtifact( gav, "target/downloads" );

        // TODO: we could tests other things like the feeds and other things, but for now this is good.
    }

}

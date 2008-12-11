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
package org.sonatype.nexus.integrationtests.proxy.nexus1111;

import junit.framework.Assert;

import org.junit.Test;
import org.mortbay.jetty.Server;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ClearCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * @author Juven Xu
 */
public class Nexus1111ProxyRemote500ErrorTest
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus1111ProxyRemote500ErrorTest()
    {
        super( "release-proxy-repo-1" );
    }

    @Test
    public void remote500Error()
        throws Exception
    {
        // first the proxy works
        downloadArtifact( "nexus1111", "artifact", "1.0", "jar", null, "target/downloads" );

        // stop the healthy server
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE );
        server.stop();

        int port = server.getPort();

        // start a server which always return HTTP-500 for get
        Server return500Server = new Server( port );
        return500Server.setHandler( new Return500Handler() );
        return500Server.start();

        // download again
        try
        {
            downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
            Assert.fail( "Should throw exception coz the remote is in a error status" );
        }
        catch ( Exception e )
        {
            // skip
        }

        // stop the error server, start the healthy server
        return500Server.stop();
        server.start();

        try
        {
            downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
            Assert.fail( "Still fails before a clear cache." );
        }
        catch ( Exception e )
        {
            // skip
        }

        // clear cache, then download
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( testRepositoryId );
        TaskScheduleUtil.runTask( ClearCacheTaskDescriptor.ID, prop );

        // the proxy is now working
        downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );

    }

}

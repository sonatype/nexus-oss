/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.proxy.nexus1111;

import junit.framework.Assert;

import org.junit.Test;
import org.mortbay.jetty.Server;
import org.restlet.data.MediaType;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * @author Juven Xu
 */
public class Nexus1111ProxyRemote500ErrorIT
    extends AbstractNexusProxyIntegrationTest
{

    public Nexus1111ProxyRemote500ErrorIT()
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
        TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, prop );

        try
        {
            // the proxy is now working <- NOT TRUE, it is auto blocked!
            downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
            Assert.fail( "Should fail, since repository is in AutoBlock mode!" );
        }
        catch ( Exception e )
        {
            // skip
        }

        // check for auto block
        RepositoryMessageUtil util =
            new RepositoryMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON, getRepositoryTypeRegistry() );

        RepositoryStatusResource status = util.getStatus( this.testRepositoryId );

        Assert.assertEquals( "Repository should be auto-blocked", ProxyMode.BLOCKED_AUTO.name(), status.getProxyMode() );

        // unblock it manually
        status.setProxyMode( ProxyMode.ALLOW.name() );
        util.updateStatus( status );

        // and now, all should go well
        downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
    }
}

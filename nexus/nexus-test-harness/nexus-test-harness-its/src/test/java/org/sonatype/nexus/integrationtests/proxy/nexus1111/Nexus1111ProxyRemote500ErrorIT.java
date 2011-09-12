/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.proxy.nexus1111;

import static org.sonatype.nexus.integrationtests.ITGroups.PROXY;

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
import org.testng.Assert;
import org.testng.annotations.Test;

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

    @Test( groups = PROXY )
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

        // This commented stuff below makes IT unpredictable
        // By starting "healthy" server, repo will eventually unblock during ExpireCache task run (more than 20sec)
        // So, I commented this out, we have NFC ITs anyway (that's what following fetch would test)
        // -- cstamas

        // // stop the error server, start the healthy server
        // return500Server.stop();
        // server.start();
        //
        // try
        // {
        // downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
        // Assert.fail( "Still fails before a clear cache." );
        // }
        // catch ( Exception e )
        // {
        // // skip
        // }

        // clear cache, then download
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
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
        // TODO: interestingly RepositoryMessageUtil.getStatus() neglects JSON here, so
        // not using it and switched back to XML as it is wired in it this util class.
        RepositoryMessageUtil util = new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );

        RepositoryStatusResource status = util.getStatus( this.testRepositoryId );

        Assert.assertEquals( status.getProxyMode(), ProxyMode.BLOCKED_AUTO.name(), "Repository should be auto-blocked" );

        // stop the error server, start the healthy server
        return500Server.stop();
        server.start();

        // unblock it manually
        // NEXUS-4410: since this issue is implemented, the lines below are not enough,
        // since NFC will still contain the artifact do be downloaded, so we need to make it manually blocked and then allow proxy
        // those steps DOES clean NFC
        status.setProxyMode( ProxyMode.BLOCKED_MANUAL.name() );
        util.updateStatus( status );
        status.setProxyMode( ProxyMode.ALLOW.name() );
        util.updateStatus( status );
        
        // and now, all should go well
        downloadArtifact( "nexus1111", "artifact", "1.1", "jar", null, "target/downloads" );
    }
}

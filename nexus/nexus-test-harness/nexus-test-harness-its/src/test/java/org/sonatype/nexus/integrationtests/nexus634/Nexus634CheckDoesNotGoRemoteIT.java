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
package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.mortbay.jetty.Server;
import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests SnapshotRemoverTask to not go remote when checking for release existence.
 * 
 * @author cstamas
 */
public class Nexus634CheckDoesNotGoRemoteIT
    extends AbstractSnapshotRemoverIT
{
    protected String localStorageDir = null;

    protected Integer proxyPort;

    protected Server server = null;

    protected TouchTrackingHandler touchTrackingHandler;

    protected RepositoryMessageUtil repositoryMessageUtil;

    public Nexus634CheckDoesNotGoRemoteIT()
        throws Exception
    {
        super();

    }
    
    @BeforeClass
    public void init() throws ComponentLookupException{
        this.localStorageDir = TestProperties.getString( "proxy.repo.base.dir" );
        this.proxyPort = TestProperties.getInteger( "proxy.server.port" );
        this.repositoryMessageUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );        
    }

    @BeforeMethod
    public void deploySnapshotArtifacts()
        throws Exception
    {
        super.deploySnapshotArtifacts();

        File remoteSnapshot = getTestFile( "remote-repo" );

        // Copying to keep an old timestamp
        FileUtils.copyDirectory( remoteSnapshot, repositoryPath );

        // update indexes?
        // RepositoryMessageUtil.updateIndexes( "nexus-test-harness-snapshot-repo" );
    }

    @BeforeMethod
    public void startProxy()
        throws Exception
    {
        touchTrackingHandler = new TouchTrackingHandler();
        server = new Server( proxyPort );
        server.setHandler( touchTrackingHandler );
        server.start();
    }

    @AfterMethod  
    public void stopProxy()
        throws Exception
    {
        if ( server != null )
        {
            server.stop();
            server = null;
            touchTrackingHandler = null;
        }
    }

    @Test
    public void keepNewSnapshots()
        throws Exception
    {
        // set proxy reposes to point here
        RepositoryProxyResource proxy =
            (RepositoryProxyResource) repositoryMessageUtil.getRepository( REPO_RELEASE_PROXY_REPO1 );
        proxy.getRemoteStorage().setRemoteStorageUrl( "http://localhost:" + proxyPort + "/" );
        repositoryMessageUtil.updateRepo( proxy );

        // expire caches
        ScheduledServicePropertyResource repoOrGroupProp = new ScheduledServicePropertyResource();
        repoOrGroupProp.setKey( "repositoryOrGroupId" );
        repoOrGroupProp.setValue( REPO_RELEASE_PROXY_REPO1 );
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( ExpireCacheTaskDescriptor.ID, repoOrGroupProp );
        Assert.assertNotNull( task );

        TaskScheduleUtil.waitForAllTasksToStop();

        // run snapshot remover
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 0, 0, true );

        TaskScheduleUtil.waitForAllTasksToStop();

        // check is proxy touched
        Assert.assertEquals(
            touchTrackingHandler.getTouchedTargets().size(), 0,
            "Proxy should not be touched! It was asked for " + touchTrackingHandler.getTouchedTargets() );
    }
}

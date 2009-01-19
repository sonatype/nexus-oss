/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus636;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

import com.thoughtworks.xstream.XStream;

/**
 * Tests evict task.
 */
public class Nexus636EvictUnusedProxiedTaskTest
    extends AbstractNexusIntegrationTest
{

    private File repositoryPath;

    private File attributesPath;

    public Nexus636EvictUnusedProxiedTaskTest()
    {
        super( REPO_RELEASE_PROXY_REPO1 );
    }

    @Before
    public void deployOldArtifacts()
        throws Exception
    {

        repositoryPath = new File( nexusBaseDir, "runtime/work/storage/" + REPO_RELEASE_PROXY_REPO1 );
        attributesPath = new File( nexusBaseDir, "runtime/work/proxy/attributes/" + REPO_RELEASE_PROXY_REPO1 );

        File repo = getTestFile( "repo" );

        FileUtils.copyDirectory( repo, repositoryPath );

        // overwrite attributes
        // FileUtils.copyDirectory( getTestFile( "attributes" ), attributesPath );

        // rebuild attributes
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "repo_" + this.getTestRepositoryId() );
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, prop );
        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );

    }

    @Test
    public void clearProxy()
        throws Exception
    {
        executeTask( "clearProxy", "repo_release-proxy-repo-1", 0 );

        File[] files = repositoryPath.listFiles();

        if ( files.length != 0 )
        {
            Assert.assertEquals( "All files should be delete from repository except the index:\n"
                + Arrays.asList( files ), 1, files.length );
            Assert.assertTrue( "The only file left should be the index.\n" + Arrays.asList( files ),
                               files[0].getAbsolutePath().endsWith( ".index" ) );
        }
    }

    @Test
    public void keepTestDeployedFiles()
        throws Exception
    {
        executeTask( "keepTestDeployedFiles", "repo_release-proxy-repo-1", 2 );

        File artifact = new File( repositoryPath, "nexus636/artifact-new/1.0/artifact-new-1.0.jar" );
        Assert.assertTrue( "The files deployed by this test should be young enought to be kept", artifact.exists() );

    }

    @Test
    public void doNotDeleteEverythingTest()
        throws Exception
    {

        executeTask( "doNotDeleteEverythingTest-1", "repo_release-proxy-repo-1", 2 );
        // expect 3 files in repo
        File groupDirectory = new File( repositoryPath, this.getTestId() );
        File[] files = groupDirectory.listFiles();
        Assert.assertEquals( "Expected 3 artifacts in repo:\n" + Arrays.asList( files ), 3, files.length );

        // edit dates on files
        File oldJar = new File( this.attributesPath, "nexus636/artifact-old/2.1/artifact-old-2.1.jar" );
        File oldPom = new File( this.attributesPath, "nexus636/artifact-old/2.1/artifact-old-2.1.pom" );

        // set date to 3 days ago
        this.changeProxyAttributeDate( oldJar, -3 );
        this.changeProxyAttributeDate( oldPom, -3 );

        // run task
        executeTask( "doNotDeleteEverythingTest-2", "repo_release-proxy-repo-1", 2 );

        // check file list
        files = groupDirectory.listFiles();
        Assert.assertEquals( "Expected 2 artifacts in repo:\n" + Arrays.asList( files ), 2, files.length );
    }

    private void executeTask( String taskName, String repository, int cacheAge )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( repository );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( cacheAge ) );

        // clean unused
        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( taskName, EvictUnusedItemsTaskDescriptor.ID, repo, age );
        Assert.assertNotNull( "Task '" + taskName + "' didn't execute!", task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
    }

    private XStream getXStream()
    {
        XStream xstream = new XStream();
        xstream.alias( "file", DefaultStorageFileItem.class );
        xstream.alias( "collection", DefaultStorageCollectionItem.class );
        xstream.alias( "link", DefaultStorageLinkItem.class );

        return xstream;
    }

    private void changeProxyAttributeDate( File attributeFile, int daysFromToday )
        throws IOException
    {
        // load file
        FileInputStream fis = new FileInputStream( attributeFile );
        // Object obj = this.getXStream().fromXML( fis );
        DefaultStorageFileItem fileItem = (DefaultStorageFileItem) this.getXStream().fromXML( fis );
        fis.close();

        Calendar cal = Calendar.getInstance();
        cal.setTime( new Date() );
        cal.add( Calendar.DATE, daysFromToday );

        // edit object
        fileItem.incrementGeneration();
        fileItem.setLastRequested( cal.getTime().getTime() );
        fileItem.setRemoteChecked( cal.getTime().getTime() );

        // save file
        FileOutputStream fos = new FileOutputStream( attributeFile );
        this.getXStream().toXML( fileItem, fos );
        fos.close();
    }

}

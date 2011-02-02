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
package org.sonatype.nexus.integrationtests.nexus636;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.EvictUnusedItemsTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

/**
 * Tests evict task.
 */
public class Nexus636EvictUnusedProxiedTaskIT
    extends AbstractNexusIntegrationTest
{

    private File repositoryPath;

    private File attributesPath;

    public Nexus636EvictUnusedProxiedTaskIT()
    {
        super( REPO_RELEASE_PROXY_REPO1 );
    }

    @BeforeMethod
    public void deployOldArtifacts()
        throws Exception
    {

        repositoryPath = new File( nexusWorkDir, "storage/" + REPO_RELEASE_PROXY_REPO1 );
        // attributesPath = new File( nexusWorkDir, "storage/" + REPO_RELEASE_PROXY_REPO1 + "/.nexus/attributes" );
        attributesPath = new File( nexusWorkDir, "proxy/attributes/" + REPO_RELEASE_PROXY_REPO1 );

        File repo = getTestFile( "repo" );

        FileUtils.copyDirectory( repo, repositoryPath );

        // overwrite attributes
        // FileUtils.copyDirectory( getTestFile( "attributes" ), attributesPath );

        // rebuild attributes
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue(  this.getTestRepositoryId() );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, prop );

    }

    @Test
    public void clearProxy()
        throws Exception
    {
        executeTask( "clearProxy", "release-proxy-repo-1", 0 );

        File[] files = repositoryPath.listFiles();

        if ( files != null && files.length != 0 )
        {
            // not true anymore, all "." (dot files) hidden files should be left in there
            // Assert.assertEquals( "All files should be delete from repository except the index:\n"
            // + Arrays.asList( files ), 1, files.length );
            // Assert.assertTrue( "The only file left should be the index.\n" + Arrays.asList( files ),
            // files[0].getAbsolutePath().endsWith( ".index" ) );

            boolean isAllDotFiles = true;

            for ( File file : files )
            {
                isAllDotFiles = isAllDotFiles && file.getName().startsWith( "." );
            }

            Assert.assertTrue( isAllDotFiles, "The only files left should be \"dotted\" files! We have: " + files );
        }
    }

    @Test
    public void keepTestDeployedFiles()
        throws Exception
    {
        executeTask( "keepTestDeployedFiles", "release-proxy-repo-1", 2 );

        File artifact = new File( repositoryPath, "nexus636/artifact-new/1.0/artifact-new-1.0.jar" );
        Assert.assertTrue( artifact.exists(), "The files deployed by this test should be young enought to be kept" );

    }

    @Test
    public void doNotDeleteEverythingTest()
        throws Exception
    {

        executeTask( "doNotDeleteEverythingTest-1", "release-proxy-repo-1", 2 );
        // expect 3 files in repo
        File groupDirectory = new File( repositoryPath, this.getTestId() );
        File[] files = groupDirectory.listFiles();
        Assert.assertEquals( files.length, 3, "Expected 3 artifacts in repo:\n" + Arrays.asList( files ) );

        // edit dates on files
        File oldJar = new File( this.attributesPath, "nexus636/artifact-old/2.1/artifact-old-2.1.jar" );
        File oldPom = new File( this.attributesPath, "nexus636/artifact-old/2.1/artifact-old-2.1.pom" );

        // set date to 3 days ago
        this.changeProxyAttributeDate( oldJar, -3 );
        this.changeProxyAttributeDate( oldPom, -3 );

        // run task
        executeTask( "doNotDeleteEverythingTest-2", "release-proxy-repo-1", 2 );

        // check file list
        files = groupDirectory.listFiles();
        Assert.assertEquals( files.length, 2, "Expected 2 artifacts in repo:\n" + Arrays.asList( files ) );
    }

    private void executeTask( String taskName, String repository, int cacheAge )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( repository );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setKey( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( cacheAge ) );

        // clean unused
        TaskScheduleUtil.runTask( taskName, EvictUnusedItemsTaskDescriptor.ID, repo, age );
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

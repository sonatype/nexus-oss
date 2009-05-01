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
package org.sonatype.nexus.integrationtests.nexus688;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus688ReindexOnRepoAdd
    extends AbstractNexusIntegrationTest
{
    private RepositoryMessageUtil messageUtil;

    private static final String INDEX_FILE = ".index/nexus-maven-repository-index.zip";

    private static final int SLEEP_TIME = 200;

    public Nexus688ReindexOnRepoAdd()
        throws ComponentLookupException
    {
        messageUtil = new RepositoryMessageUtil(
            this.getXMLXStream(),
            MediaType.APPLICATION_XML,
            getRepositoryTypeRegistry() );
    }

    @Test
    public void repoTestIndexable()
        throws Exception
    {

        // create a repo
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "nexus688-repoTestIndexable" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForTasks();

        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );

        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }

    @Test
    public void repoTestNotIndexable()
        throws Exception
    {

        // create a repo
        RepositoryResource resource = new RepositoryResource();

        resource.setId( "nexus688-repoTestNotIndexable" );
        resource.setRepoType( "hosted" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setAllowWrite( true );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForTasks();

        // check to see if it has an index to download
        try
        {
            this.downloadIndexFromRepository( resource.getId() );
            Assert.fail( "Expected a 404, FileNotFoundException." );
        }
        catch ( FileNotFoundException e )
        {
            // expected 404
        }
    }

    @Test
    public void proxyRepoTestIndexableWithInvalidURL()
        throws Exception
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestIndexableWithInvalidURL" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForTasks();

        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );

        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }

    @Test
    public void proxyRepoTestIndexable()
        throws Exception
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestIndexable" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setAllowWrite( true );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForTasks();

        // check to see if it has an index to download
        File indexFile = this.downloadIndexFromRepository( resource.getId() );

        // if the above line didn't throw a FileNotFound, we are good to go, but check anyway.
        Assert.assertNotNull( "Downloaded index file was null.", indexFile );
        Assert.assertTrue( "Downloaded index does not exists", indexFile.exists() );
    }

    @Test
    public void proxyRepoTestNotIndexable()
        throws Exception
    {

        // create a repo
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setId( "nexus688-proxyRepoTestNotIndexable" );
        resource.setRepoType( "proxy" );
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( "release" );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setAllowWrite( true );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForTasks();

        // check to see if it has an index to download
        try
        {
            this.downloadIndexFromRepository( resource.getId() );
            Assert.fail( "Expected a 404, FileNotFoundException." );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
            // expected 404
        }
    }

    private File downloadIndexFromRepository( String repoId )
        throws MalformedURLException,
            IOException
    {
        String repositoryUrl = this.getRepositoryUrl( repoId );
        URL url = new URL( repositoryUrl + INDEX_FILE );
        return this.downloadFile( url, "target/downloads/index.zip" );
    }

}

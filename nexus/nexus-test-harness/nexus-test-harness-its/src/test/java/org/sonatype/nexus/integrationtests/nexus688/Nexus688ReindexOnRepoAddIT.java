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
import java.net.URL;
import java.util.Arrays;

import junit.framework.Assert;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus688ReindexOnRepoAddIT
    extends AbstractNexusIntegrationTest
{
    private RepositoryMessageUtil messageUtil;

    // Indexer stopped publishing "old" index for good
    // private static final String OLD_INDEX_FILE = ".index/nexus-maven-repository-index.zip";
    private static final String NEW_INDEX_FILE = ".index/nexus-maven-repository-index.gz";

    private static final String INDEX_PROPERTIES = ".index/nexus-maven-repository-index.properties";

    public Nexus688ReindexOnRepoAddIT()
        throws ComponentLookupException
    {
        messageUtil =
            new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML,
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
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        resource.setExposed( true );
        // invalid for hosted repo resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        this.downloadIndexFromRepository( resource.getId(), true );
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
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        resource.setExposed( true );
        // invalid for hosted repo resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        this.downloadIndexFromRepository( resource.getId(), false );
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
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        resource.setExposed( true );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL/" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        this.downloadIndexFromRepository( resource.getId(), true );
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
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( true );
        resource.setExposed( true );
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL/" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        this.downloadIndexFromRepository( resource.getId(), true );
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
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        resource.setChecksumPolicy( "IGNORE" );
        resource.setBrowseable( true );
        resource.setIndexable( false );
        resource.setExposed( true );
        resource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://INVALID-URL/" );
        resource.setRemoteStorage( remoteStorage );

        // this also validates
        this.messageUtil.createRepository( resource );

        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );

        this.downloadIndexFromRepository( resource.getId(), false );
    }

    private void downloadIndexFromRepository( String repoId, boolean shouldSucceed )
        throws Exception
    {
        String repositoryUrl = this.getRepositoryUrl( repoId );
        
        URL url = null;

        // nexus does not publish old indexes anymore
        // URL url = new URL( repositoryUrl + OLD_INDEX_FILE );
        // downloadFromRepository( url, "target/downloads/index.zip", repoId, shouldSucceed );
        // url = new URL( repositoryUrl + OLD_INDEX_FILE + ".sha1" );
        // downloadFromRepository( url, "target/downloads/index.zip.sha1", repoId, shouldSucceed );
        // url = new URL( repositoryUrl + OLD_INDEX_FILE + ".md5" );
        // downloadFromRepository( url, "target/downloads/index.zip.md5", repoId, shouldSucceed );

        url = new URL( repositoryUrl + NEW_INDEX_FILE );
        downloadFromRepository( url, "target/downloads/index.gz", repoId, shouldSucceed );
        url = new URL( repositoryUrl + NEW_INDEX_FILE + ".sha1" );
        downloadFromRepository( url, "target/downloads/index.gz.sha1", repoId, shouldSucceed );
        url = new URL( repositoryUrl + NEW_INDEX_FILE + ".md5" );
        downloadFromRepository( url, "target/downloads/index.gz.md5", repoId, shouldSucceed );

        url = new URL( repositoryUrl + INDEX_PROPERTIES );
        downloadFromRepository( url, "target/downloads/index.properties", repoId, shouldSucceed );
        url = new URL( repositoryUrl + INDEX_PROPERTIES + ".sha1" );
        downloadFromRepository( url, "target/downloads/index.properties.sha1", repoId, shouldSucceed );
        url = new URL( repositoryUrl + INDEX_PROPERTIES + ".md5" );
        downloadFromRepository( url, "target/downloads/index.properties.md5", repoId, shouldSucceed );
    }

    private void downloadFromRepository( URL url, String target, String repoId, boolean shouldSucceed )
        throws Exception
    {
        try
        {
            this.downloadFile( url, target );
            if ( !shouldSucceed )
            {
                Assert.fail( "Expected 404, but file was downloaded" );
            }
        }
        catch ( FileNotFoundException e )
        {
            if ( shouldSucceed )
            {
                Assert.fail( e.getMessage() + "\n Found files:\n"
                    + Arrays.toString( new File( nexusWorkDir, "storage/" + repoId + "/.index" ).listFiles() ) );
            }
        }
    }

}

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
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus688ReindexOnRepoAdd
    extends AbstractNexusIntegrationTest
{
    private RepositoryMessageUtil messageUtil;

    private static final String OLD_INDEX_FILE = ".index/nexus-maven-repository-index.zip";

    private static final String NEW_INDEX_FILE = ".index/nexus-maven-repository-index.gz";

    private static final String INDEX_PROPERTIES = ".index/nexus-maven-repository-index.properties";

    public Nexus688ReindexOnRepoAdd()
        throws ComponentLookupException
    {
        messageUtil =
            new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, getRepositoryTypeRegistry() );
    }

    private void downloadIndexFromRepository( String repoId, boolean shouldSucceed )
        throws Exception
    {
        String repositoryUrl = this.getRepositoryUrl( repoId );

        URL url = new URL( repositoryUrl + OLD_INDEX_FILE );
        downloadFromRepository( url, "target/downloads/index.zip", repoId, shouldSucceed );
        url = new URL( repositoryUrl + OLD_INDEX_FILE + ".sha1" );
        downloadFromRepository( url, "target/downloads/index.zip.sha1", repoId, shouldSucceed );
        url = new URL( repositoryUrl + OLD_INDEX_FILE + ".md5" );
        downloadFromRepository( url, "target/downloads/index.zip.md5", repoId, shouldSucceed );

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

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
package org.sonatype.nexus.integrationtests.proxy.nexus178;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import junit.framework.Assert;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 * Create an http server. Create a proxy repo to http server. Test if connection works. block proxy, change file on http
 * server. test connection. check to make sure file is the one expected. Delete file from nexus.
 */
public class Nexus178BlockProxyDownloadIT
    extends AbstractNexusProxyIntegrationTest
{

    public static final String TEST_RELEASE_REPO = "release-proxy-repo-1";

    public Nexus178BlockProxyDownloadIT()
    {
        super( TEST_RELEASE_REPO );
    }

    @Test
    public void blockProxy()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "block-proxy-download-test", "1.1.a", null, "jar", 0, new Date().getTime(),
                     "Simple Test Artifact", false, false, null, false, null );

        // download file
        File originalFile = this.downloadArtifact( gav, "target/downloads/original" );

        // blockProxy
        this.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, true );

        // change file on server
        File localFile = this.getLocalFile( TEST_RELEASE_REPO, gav );
        // backup file on server
        this.backupFile( localFile );
        try
        {
            // we need to edit the file now, (its just a text file)
            this.changeFile( localFile );

            // redownload file
            File newFile = this.downloadArtifact( gav, "target/downloads/new" );

            // check to see if file matches original file
            Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, newFile ) );

            // check to see if file does match new file.
            Assert.assertFalse( FileTestingUtils.compareFileSHA1s( originalFile, localFile ) );

            // if we don't unblock the proxy the other tests will be mad
            this.setBlockProxy( this.getBaseNexusUrl(), TEST_RELEASE_REPO, false );
        }
        finally
        {
            this.revertFile( localFile );
        }

    }

    private void backupFile( File file )
        throws IOException
    {
        File backupFile = new File( file.getParentFile(), file.getName() + ".backup" );

        FileUtils.copyFile( file, backupFile );
    }

    private void revertFile( File file )
        throws IOException
    {
        File backupFile = new File( file.getParentFile(), file.getName() + ".backup" );

        FileUtils.copyFile( backupFile, file );
    }

    private void changeFile( File file )
        throws IOException
    {
        PrintWriter printWriter = new PrintWriter( new FileWriter( file ) );
        printWriter.println( "I just changed the content of this file!" );
        printWriter.close();
    }

}

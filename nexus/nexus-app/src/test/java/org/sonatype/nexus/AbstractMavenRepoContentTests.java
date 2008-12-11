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
package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryWalkListener;
import org.codehaus.plexus.util.DirectoryWalker;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.DefaultNexus;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.MavenRepository;

/**
 * Tests that needs some repo content and are Maven related.
 * 
 * @author cstamas
 */
public class AbstractMavenRepoContentTests
    extends AbstractNexusTestCase
{
    protected DefaultNexus defaultNexus;

    protected MavenRepository snapshots;

    protected MavenRepository releases;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        getContainer().getLoggerManager().setThresholds( Logger.LEVEL_DEBUG );

        defaultNexus = (DefaultNexus) lookup( Nexus.class );

        // get a snapshots hosted repo
        snapshots = (MavenRepository) defaultNexus.getRepository( "snapshots" );

        // get a releases hosted repo
        releases = (MavenRepository) defaultNexus.getRepository( "releases" );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void fillInRepo()
        throws Exception
    {
        final File sourceSnapshotsRoot = new File( getBasedir(), "src/test/resources/reposes/snapshots" )
            .getAbsoluteFile();

        final URL snapshotsRootUrl = new URL( snapshots.getLocalUrl() );

        final File snapshotsRoot = new File( snapshotsRootUrl.toURI() ).getAbsoluteFile();

        copyDirectory( sourceSnapshotsRoot, snapshotsRoot );

        final File sourceReleasesRoot = new File( getBasedir(), "src/test/resources/reposes/releases" );

        final URL releaseRootUrl = new URL( releases.getLocalUrl() );

        final File releasesRoot = new File( releaseRootUrl.toURI() );

        copyDirectory( sourceReleasesRoot, releasesRoot );

        // This above is possible, since SnapshotRemover is not using index, hence we can manipulate the content
        // "from behind"

        // but clear caches
        snapshots.clearCaches( RepositoryItemUid.PATH_ROOT );
        releases.clearCaches( RepositoryItemUid.PATH_ROOT );
    }

    protected void copyDirectory( final File from, final File to )
        throws IOException
    {
        DirectoryWalker w = new DirectoryWalker();

        w.setBaseDir( from );

        w.addSCMExcludes();

        w.addDirectoryWalkListener( new DirectoryWalkListener()
        {
            public void debug( String message )
            {
            }

            public void directoryWalkStarting( File basedir )
            {
            }

            public void directoryWalkStep( int percentage, File file )
            {
                if ( !file.isFile() )
                {
                    return;
                }

                try
                {
                    String path = file.getAbsolutePath().substring( from.getAbsolutePath().length() );

                    FileUtils.copyFile( file, new File( to, path ) );
                }
                catch ( IOException e )
                {
                    throw new IllegalStateException( "Cannot copy dirtree.", e );
                }
            }

            public void directoryWalkFinished()
            {
            }
        } );

        w.scan();
    }

}

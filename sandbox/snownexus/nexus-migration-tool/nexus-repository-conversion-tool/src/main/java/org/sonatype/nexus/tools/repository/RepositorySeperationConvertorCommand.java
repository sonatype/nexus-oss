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
package org.sonatype.nexus.tools.repository;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 *
 * @author Juven Xu
 *
 */
@Component( role = ConvertorCommand.class, hint = RepositorySeperationConvertorCommand.ID )
public class RepositorySeperationConvertorCommand
    implements ConvertorCommand
{

    public static final String ID = "RepositorySeperationConvertorCommand";

    @Requirement
    private RepositoryConvertorFileHelper repositoryConvertorFileHelper;

    private boolean isMove = false;

    private File repository;

    private File releaseRepository;

    private File snapshotRepository;

    private FileFilter filter;

    public void setMove( boolean isMove )
    {
        this.isMove = isMove;
    }

    public void setRepository( File repository )
    {
        this.repository = repository;
    }

    public void setReleaseRepository( File releaseRepository )
    {
        this.releaseRepository = releaseRepository;
    }

    public void setSnapshotRepository( File snapshotRepository )
    {
        this.snapshotRepository = snapshotRepository;
    }

    public void execute( List<File> artifactVersions )
        throws IOException
    {
        for ( File version : artifactVersions )
        {
            if ( isMove )
            {
                moveToTargetRepository( version );
            }
            else
            {
                copyToTargetRepository( version );
            }
        }
    }

    /**
     * copy the version folder to its target repository according to it's version type
     *
     * @param versionFolder A folder in repository, normally its name is a version of an artifact
     * @throws IOException
     */
    private void copyToTargetRepository( File versionFolder )
        throws IOException
    {
        repositoryConvertorFileHelper.copy(
            versionFolder,
            getTargetRepository( versionFolder ),
            getCoordinatePath( versionFolder ), filter );
    }

    /**
     * move the version folder to its target repository according to it's version type
     *
     * @param versionFolder A folder in repository, normally its name is a version of an artifact
     * @throws IOException
     */
    private void moveToTargetRepository( File versionFolder )
        throws IOException
    {
        repositoryConvertorFileHelper.move(
            versionFolder,
            getTargetRepository( versionFolder ),
            getCoordinatePath( versionFolder ), filter );
    }

    private File getTargetRepository( File versionFolder )
    {
        if ( isSnapshot( versionFolder.getName() ) )
        {
            return snapshotRepository;
        }
        else
        {
            return releaseRepository;
        }
    }

    /**
     * @param versionFolder
     * @return The path from repository root to the parent of our version folder
     */
    private String getCoordinatePath( File versionFolder )
    {
        String temp = versionFolder.getAbsolutePath().substring( repository.getAbsolutePath().length() );

        return temp.substring( 0, temp.length() - versionFolder.getName().length() );
    }

    private boolean isSnapshot( String version )
    {
        if ( version.endsWith( "SNAPSHOT" ) )
        {
            return true;
        }
        return false;
    }

    public void setFilter( FileFilter filter )
    {
        this.filter = filter;
    }

}

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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * @author Juven Xu
 */
@Component( role = RepositoryConvertor.class )
public class DefaultRepositoryConvertor
    implements RepositoryConvertor
{

    public static final String VERSION_REGEX = "^[0-9].*$";

    @Requirement( role = ConvertorCommand.class, hint = RepositorySeperationConvertorCommand.ID )
    private RepositorySeperationConvertorCommand repositorySeperationConvertorCommand;

    private File currentRepository;

    private File releaseRepository;

    private File snapshotRepository;

    private List<ConvertorCommand> convertorCommands = new ArrayList<ConvertorCommand>();

    public void convertRepositoryWithCopy( File repository, File releasedTargetPath, File snapshotTargetPath,
        FileFilter filter )
        throws IOException
    {
        setUp( repository, releasedTargetPath, snapshotTargetPath, false, filter );
    }

    public void convertRepositoryWithMove( File repository, File releasedTargetPath, File snapshotTargetPath,
        FileFilter filter )
        throws IOException
    {
        setUp( repository, releasedTargetPath, snapshotTargetPath, true, filter );

        deleteCurrentRepository();
    }

    private void setUp( File repository, File releasedTargetPath, File snapshotTargetPath, boolean move,
        FileFilter filter )
        throws IOException
    {
        currentRepository = repository;

        releaseRepository = releasedTargetPath;

        snapshotRepository = snapshotTargetPath;

        releaseRepository.mkdir();

        snapshotRepository.mkdir();

        convertorCommands.clear();

        repositorySeperationConvertorCommand.setRepository( currentRepository );
        repositorySeperationConvertorCommand.setReleaseRepository( releaseRepository );
        repositorySeperationConvertorCommand.setSnapshotRepository( snapshotRepository );
        repositorySeperationConvertorCommand.setMove( move );
        repositorySeperationConvertorCommand.setFilter( filter );

        convertorCommands.add( repositorySeperationConvertorCommand );

        List<File> operand = new LinkedList<File>();

        collectOperandRecursive( currentRepository, operand );

        executeCommands( operand );

    }

    private void executeCommands( List<File> operatableFiles )
        throws IOException
    {
        for ( ConvertorCommand convertorCommand : convertorCommands )
        {
            convertorCommand.execute( operatableFiles );
        }
    }

    private void collectOperandRecursive( File file, List<File> operatableFiles )
        throws IOException
    {
        if ( !file.isDirectory() )
        {
            return;
        }

        if ( hasArtifacts( file ) )
        {
            operatableFiles.add( file );

            return;
        }

        for ( File subFile : file.listFiles() )
        {
            collectOperandRecursive( subFile, operatableFiles );
        }
    }

    private boolean hasArtifacts( File dir )
    {
        for ( File file : dir.listFiles() )
        {
            if ( file.isFile() && file.getName().endsWith( "pom" ) )
            {
                return true;
            }
        }

        return false;
    }

    private void deleteCurrentRepository()
    {
        deleteFile( currentRepository );
    }

    private void deleteFile( File file )
    {
        if ( file.isDirectory() )
        {
            for ( File subFile : file.listFiles() )
            {
                deleteFile( subFile );
            }
        }
        file.delete();
    }

    public void convertRepositoryWithCopy( File repository, File targetPath )
        throws IOException
    {
        File releasedTargetPath = new File( targetPath, repository.getName() + SUFFIX_RELEASES );

        File snapshotTargetPath = new File( targetPath, repository.getName() + SUFFIX_SNAPSHOTS );

        convertRepositoryWithCopy( repository, releasedTargetPath, snapshotTargetPath, null );
    }

    public void convertRepositoryWithMove( File repository, File targetPath )
        throws IOException
    {
        File releasedTargetPath = new File( targetPath, repository.getName() + SUFFIX_RELEASES );

        File snapshotTargetPath = new File( targetPath, repository.getName() + SUFFIX_SNAPSHOTS );

        convertRepositoryWithMove( repository, releasedTargetPath, snapshotTargetPath, null );
    }

}

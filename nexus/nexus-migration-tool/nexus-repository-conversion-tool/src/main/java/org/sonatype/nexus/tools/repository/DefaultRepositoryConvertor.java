/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

        if ( file.getName().matches( VERSION_REGEX ) || hasArtifacts( file ) )
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
            if ( file.isFile() && ( file.getName().endsWith( "pom" ) || file.getName().endsWith( "jar" ) ) )
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

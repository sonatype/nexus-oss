package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractRepositoryFolderCleaner
    implements RepositoryFolderCleaner
{
    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    protected Logger getLogger()
    {
        return logger;
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    /**
     * Delete the file forever, or just keep it by renaming it (hence, will not be used anymore).
     * 
     * @param file file to be deleted
     * @param deleteForever if it's true, delete the file forever, if it's false, move the file to trash
     * @throws IOException
     */
    protected void delete( final File file, final boolean deleteForever )
        throws IOException
    {
        if ( !deleteForever )
        {
            File basketFile = new File( getApplicationConfiguration().getWorkingDirectory( "trash" ), file.getName() );

            if ( file.isDirectory() )
            {
                FileUtils.mkdir( basketFile.getAbsolutePath() );

                FileUtils.copyDirectoryStructure( file, basketFile );
            }
            else
            {
                FileUtils.copyFile( file, basketFile );
            }
        }

        FileUtils.forceDelete( file );
    }
}

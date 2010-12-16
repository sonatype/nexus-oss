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
        if ( file.isDirectory() )
        {
            deleteFilesRecursively( file );
        }
        else
        {
            FileUtils.forceDelete( file );
        }
    }

    // This method prevents locked files on Windows from not allowing to delete unlocked files, i.e., it will keep on
    // deleting other files even if it reaches a locked file first.
    private void deleteFilesRecursively( File folder )
    {
        // First check if it's a directory to avoid future misuse.
        if ( folder.isDirectory() )
        {
            File[] files = folder.listFiles();
            for ( File file : files )
            {
                if ( file.isDirectory() )
                {
                    deleteFilesRecursively( file );
                }
                else
                {
                    try
                    {
                        FileUtils.forceDelete( file );
                    }
                    catch ( IOException ioe )
                    {
                        ioe.printStackTrace();
                    }
                }
            }
            // After cleaning the files, tries to delete the containing folder.
            try
            {
                FileUtils.forceDelete( folder );
            }
            catch ( IOException ioe )
            {
                // If the folder cannot be deleted it means there are locked files in it. But we don't need to log it
                // here once the file locks had already been detected and logged in the for loop above.
            }
        }
        else
        {
            try
            {
                FileUtils.forceDelete( folder );
            }
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
            }
        }
    }
}

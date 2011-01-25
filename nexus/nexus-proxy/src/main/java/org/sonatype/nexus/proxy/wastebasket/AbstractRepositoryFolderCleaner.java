/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
    public static final String GLOBAL_TRASH_KEY = "trash";

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
            File basketFile =
                new File( getApplicationConfiguration().getWorkingDirectory( GLOBAL_TRASH_KEY ), file.getName() );

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
    protected static void deleteFilesRecursively( File folder )
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

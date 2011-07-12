/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.plugin.migration.artifactory.util.XStreamUtil;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractArtifactoryMigrationPlexusResource
    extends AbstractNexusPlexusResource
{
    public AbstractArtifactoryMigrationPlexusResource()
    {
        super();
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        XStreamUtil.configureMigration(xstream);
    }

    protected File validateBackupFileLocation( String fileLocation )
        throws ResourceException
    {
        File file = new File( fileLocation );

        if ( !file.exists() )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid File Location: "
                + file.getAbsolutePath() );
        }

        if ( file.isFile() )
        {
            ZipFile zip;
            try
            {
                zip = new ZipFile( file );
                zip.close();
            }
            catch ( ZipException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid file format. Is not a Zip. "
                    + e.getMessage() );
            }
            catch ( IOException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Unable to read file. " + e.getMessage() );
            }
        }

        return file;
    }

    protected InputStream getConfigurationFile( File backup, String filename )
        throws IOException, ZipException, FileNotFoundException, ResourceException
    {
        ZipFile zipFile = null;

        if ( backup.isFile() )
        {
            zipFile = new ZipFile( backup );
        }

        final InputStream cfg;
        if ( zipFile != null )
        {
            ZipEntry cfgEntry = zipFile.getEntry( filename );
            if ( cfgEntry == null )
            {
                cfg = null;
            }
            else
            {
                cfg = zipFile.getInputStream( cfgEntry );
            }
        }
        else
        {
            File cfgFile = new File( backup, filename );
            if ( !cfgFile.exists() )
            {
                cfg = null;
            }
            else
            {
                cfg = new FileInputStream( cfgFile );
            }
        }

        if ( cfg == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Artifactory backup is invalid, missing: '"
                + filename + "'" );
        }
        return cfg;

    }

}

package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.plugin.metadata.GAVCoordinate;

public abstract class AbstractWritableFileNexusPluginRepository
    extends AbstractFileNexusPluginRepository
    implements NexusWritablePluginRepository
{

    @SuppressWarnings( "unchecked" )
    public boolean installPluginBundle( File bundle )
        throws IOException
    {
        ZipFile bundleFile = new ZipFile( bundle );

        try
        {
            Enumeration entries = bundleFile.entries();

            FileOutputStream fos = null;

            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if ( entry.isDirectory() )
                {
                    // skip it
                }
                else
                {
                    File file = new File( getNexusPluginsDirectory(), entry.getName() );

                    file.getParentFile().mkdirs();

                    try
                    {
                        fos = new FileOutputStream( file );

                        IOUtil.copy( bundleFile.getInputStream( entry ), fos );
                    }
                    finally
                    {
                        IOUtil.close( fos );
                    }
                }
            }
        }
        finally
        {
            bundleFile.close();
        }

        return true;
    }

    public boolean deletePluginBundle( GAVCoordinate coordinates )
        throws IOException
    {
        try
        {
            File pluginFolder = getPluginFolder( coordinates );
            if ( pluginFolder.isDirectory() )
            {
                FileUtils.deleteDirectory( pluginFolder );

                return true;
            }
        }
        catch ( NoSuchPluginRepositoryArtifactException e )
        {
            // nothing to delete
        }

        return false;
    }
}

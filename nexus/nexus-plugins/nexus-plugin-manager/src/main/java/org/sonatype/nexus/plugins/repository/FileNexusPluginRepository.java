package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.nexus.plugins.PluginCoordinates;

/**
 * A very trivial "local" repository implementation for Nexus plugins. The layout is:
 * 
 * <pre>
 * \/G
 *  \/A
 *   \/V
 *     plugin.jar
 *     \/dependencies
 *       dep1.jar
 *       dep2.jar
 *       ...
 * </pre>
 * 
 * @author cstamas
 */
@Component( role = NexusPluginRepository.class, hint = "file" )
public class FileNexusPluginRepository
    implements NexusPluginRepository
{
    @Configuration( value = "${nexus-work}/plugin-repository" )
    private File nexusPluginsDirectory;

    protected File getNexusPluginsDirectory()
    {
        if ( !nexusPluginsDirectory.exists() )
        {
            nexusPluginsDirectory.mkdirs();
        }

        return nexusPluginsDirectory;
    }

    public Collection<PluginCoordinates> findAvailablePlugins()
    {
        ArrayList<PluginCoordinates> result = new ArrayList<PluginCoordinates>();

        File root = getNexusPluginsDirectory();

        if ( root.isDirectory() )
        {
            File[] groupIds = root.listFiles();

            if ( groupIds != null )
            {
                for ( File groupId : groupIds )
                {
                    if ( groupId.isDirectory() )
                    {
                        File[] artifactIds = groupId.listFiles();

                        if ( artifactIds != null )
                        {
                            for ( File artifactId : artifactIds )
                            {
                                if ( artifactId.isDirectory() )
                                {
                                    File[] versions = artifactId.listFiles();

                                    if ( versions != null )
                                    {
                                        for ( File version : versions )
                                        {
                                            if ( version.isDirectory() )
                                            {
                                                PluginCoordinates coord = new PluginCoordinates();

                                                coord.setGroupId( groupId.getName() );

                                                coord.setArtifactId( artifactId.getName() );

                                                coord.setVersion( version.getName() );

                                                File pluginFile = new File( version, getPluginFileName( coord ) );

                                                if ( pluginFile.isFile() )
                                                {
                                                    result.add( coord );
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public File resolvePlugin( PluginCoordinates coordinates )
    {
        File pluginFile = new File( getPluginFolder( coordinates ), getPluginFileName( coordinates ) );

        return pluginFile;
    }

    public Collection<File> resolvePluginDependencies( PluginCoordinates coordinates )
    {
        ArrayList<File> result = new ArrayList<File>();

        File depsFolder = getPluginDependenciesFolder( coordinates );

        if ( depsFolder.isDirectory() )
        {
            File[] deps = depsFolder.listFiles();

            if ( deps != null )
            {
                for ( File dep : deps )
                {
                    result.add( dep );
                }
            }
        }

        return result;
    }

    // ==

    protected File getPluginFolder( PluginCoordinates coordinates )
    {
        return new File( getNexusPluginsDirectory(), coordinates.getGroupId() + "/" + coordinates.getArtifactId() + "/"
            + coordinates.getVersion() );
    }

    protected File getPluginDependenciesFolder( PluginCoordinates coordinates )
    {
        File depsFolder = new File( getPluginFolder( coordinates ), "dependencies" );

        return depsFolder;
    }

    protected String getPluginFileName( PluginCoordinates coordinates )
    {
        return coordinates.getArtifactId() + "-" + coordinates.getVersion() + ".jar";
    }
}

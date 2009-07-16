package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugins.PluginCoordinates;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.plugin.metadata.GAVCoordinate;

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
 * TODO: make this proper M2 layout and not "make up" some 3rd kind!
 * 
 * @author cstamas
 */
public abstract class AbstractFileNexusPluginRepository
    implements NexusPluginRepository
{
    protected abstract File getNexusPluginsDirectory();

    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    public Collection<PluginRepositoryArtifact> findAvailablePlugins()
    {
        ArrayList<PluginRepositoryArtifact> result = new ArrayList<PluginRepositoryArtifact>();

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
                                                PluginCoordinates coord =
                                                    new PluginCoordinates( groupId.getName(), artifactId.getName(),
                                                                           version.getName() );

                                                File pluginFile = new File( version, getPluginFileName( coord ) );

                                                if ( pluginFile.isFile() )
                                                {
                                                    PluginRepositoryArtifact art = new PluginRepositoryArtifact();

                                                    art.setNexusPluginRepository( this );

                                                    art.setCoordinate( coord );

                                                    art.setFile( pluginFile );

                                                    result.add( art );
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

    public PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
    {
        File pluginFile = new File( getPluginFolder( coordinates ), getPluginFileName( coordinates ) );

        if ( pluginFile.isFile() )
        {
            PluginRepositoryArtifact art = new PluginRepositoryArtifact();

            art.setNexusPluginRepository( this );

            art.setCoordinate( coordinates );

            art.setFile( pluginFile );

            return art;
        }
        else
        {
            return null;
        }
    }

    public PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact dependant,
                                                               GAVCoordinate coordinates )
    {
        File pluginFile =
            new File( getPluginDependenciesFolder( dependant.getCoordinate() ), getPluginFileName( coordinates ) );

        if ( pluginFile.isFile() )
        {
            PluginRepositoryArtifact art = new PluginRepositoryArtifact();

            art.setNexusPluginRepository( this );

            art.setCoordinate( coordinates );

            art.setFile( pluginFile );

            return art;
        }
        else
        {
            return null;
        }
    }

    // ==

    protected File getPluginFolder( GAVCoordinate coordinates )
    {
        return new File( getNexusPluginsDirectory(), coordinates.getGroupId() + "/" + coordinates.getArtifactId() + "/"
            + coordinates.getVersion() );
    }

    protected File getPluginDependenciesFolder( GAVCoordinate coordinates )
    {
        File depsFolder = new File( getPluginFolder( coordinates ), "dependencies" );

        return depsFolder;
    }

    protected String getPluginFileName( GAVCoordinate coordinates )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( coordinates.getArtifactId() ).append( "-" ).append( coordinates.getVersion() );

        if ( StringUtils.isNotBlank( coordinates.getClassifier() ) )
        {
            sb.append( "-" ).append( coordinates.getClassifier() );
        }

        sb.append( "." ).append( artifactPackagingMapper.getExtensionForPackaging( coordinates.getType() ) );

        return sb.toString();
    }
}

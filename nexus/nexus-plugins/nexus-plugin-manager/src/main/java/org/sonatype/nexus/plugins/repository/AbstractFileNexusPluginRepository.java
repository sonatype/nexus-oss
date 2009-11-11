package org.sonatype.nexus.plugins.repository;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.proxy.maven.ArtifactPackagingMapper;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.plugins.model.io.xpp3.PluginModelXpp3Reader;

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
    private Logger logger;

    @Requirement
    private ArtifactPackagingMapper artifactPackagingMapper;

    protected Logger getLogger()
    {
        return logger;
    }

    public Map<GAVCoordinate, PluginMetadata> findAvailablePlugins()
    {
        HashMap<GAVCoordinate, PluginMetadata> result = new HashMap<GAVCoordinate, PluginMetadata>();

        File root = getNexusPluginsDirectory();

        if ( root.isDirectory() )
        {
            File[] pluginFolders = root.listFiles();

            if ( pluginFolders != null )
            {
                for ( File pluginFolder : pluginFolders )
                {
                    if ( pluginFolder.isDirectory() )
                    {
                        File pluginFile = new File( pluginFolder, pluginFolder.getName() + ".jar" );

                        if ( pluginFile.isFile() )
                        {
                            PluginMetadata md = getMetadataFromFile( pluginFile );

                            if ( md != null )
                            {
                                GAVCoordinate coord =
                                    new GAVCoordinate( md.getGroupId(), md.getArtifactId(), md.getVersion() );

                                result.put( coord, md );
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public PluginRepositoryArtifact resolveArtifact( GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException
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
            throw new NoSuchPluginRepositoryArtifactException( this, coordinates );
        }
    }

    public PluginRepositoryArtifact resolveDependencyArtifact( PluginRepositoryArtifact dependant,
        GAVCoordinate coordinates )
        throws NoSuchPluginRepositoryArtifactException
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
            throw new NoSuchPluginRepositoryArtifactException( this, coordinates );
        }
    }

    // ==

    protected PluginMetadata getMetadataFromFile( File pluginFile )
    {
        ZipFile jar = null;

        try
        {
            jar = new ZipFile( pluginFile );

            ZipEntry entry = jar.getEntry( "META-INF/nexus/plugin.xml" );

            Reader reader = null;

            try
            {
                if ( entry == null )
                {
                    return null;
                }

                reader = ReaderFactory.newXmlReader( jar.getInputStream( entry ) );

                PluginModelXpp3Reader pdreader = new PluginModelXpp3Reader();

                PluginMetadata md = pdreader.read( reader );

                return md;
            }
            finally
            {
                IOUtil.close( reader );
            }
        }
        catch ( XmlPullParserException e )
        {
            getLogger().error(
                "Invalid plugin metadata, skipping plugin file \"" + pluginFile.getAbsolutePath() + "\"!", e );

            return null;
        }
        catch ( IOException e )
        {
            getLogger().error(
                "Got IOException while extracting the plugin metadata, skipping plugin file \""
                    + pluginFile.getAbsolutePath() + "\"!", e );

            return null;
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( IOException e )
                {
                    // nothing
                }
            }
        }
    }

    protected File getPluginFolder( GAVCoordinate coordinates )
    {
        return new File( getNexusPluginsDirectory(), coordinates.getArtifactId() + "-" + coordinates.getVersion() );
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

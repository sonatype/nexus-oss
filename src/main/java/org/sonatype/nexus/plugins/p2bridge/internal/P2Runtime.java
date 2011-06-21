package org.sonatype.nexus.plugins.p2bridge.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.core.runtime.internal.adaptor.EclipseEnvironmentInfo;
import org.sonatype.eclipse.bridge.EclipseBridge;
import org.sonatype.eclipse.bridge.EclipseInstance;
import org.sonatype.eclipse.bridge.EclipseLocation;
import org.sonatype.eclipse.bridge.EclipseLocationFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.repository.NoSuchPluginRepositoryArtifactException;
import org.sonatype.nexus.plugins.repository.PluginRepositoryArtifact;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.plugin.metadata.GAVCoordinate;

@Named
@Singleton
class P2Runtime
{
    private EclipseInstance eclipse;

    private final EclipseLocationFactory eclipseLocationFactory;

    private final EclipseBridge eclipseBridge;

    private final PluginRepositoryManager pluginRepositoryManager;

    private final ApplicationConfiguration applicationConfiguration;

    @Inject
    public P2Runtime( final EclipseBridge eclipseBridge, final EclipseLocationFactory eclipseLocationFactory,
                      final PluginRepositoryManager pluginRepositoryManager,
                      final ApplicationConfiguration applicationConfiguration )
    {
        this.eclipseBridge = eclipseBridge;
        this.eclipseLocationFactory = eclipseLocationFactory;
        this.pluginRepositoryManager = pluginRepositoryManager;
        this.applicationConfiguration = applicationConfiguration;
    }

    EclipseInstance get()
    {
        initialize();
        return eclipse;
    }

    P2Runtime shutdown()
    {
        return this;
    }

    private synchronized void initialize()
    {
        if ( eclipse != null )
        {
            return;
        }
        final File pluginDir = getP2RepositoryPluginDir();
        final EclipseLocation eclipseLocation =
            eclipseLocationFactory.createStaticEclipseLocation( new File( pluginDir, "p2-runtime/eclipse" ) );
        eclipse = eclipseBridge.createInstance( eclipseLocation );
        try
        {
            {
                // TODO is this really necessary?
                final File secureStorage =
                    new File( applicationConfiguration.getConfigurationDirectory(), "eclipse.secure_storage" );
                EclipseEnvironmentInfo.setAppArgs( new String[] { "-eclipse.keyring", secureStorage.getAbsolutePath() } );
            }
            eclipse.start( initParams( pluginDir ) );
            final File[] bundles = new File( pluginDir, "p2-runtime/bundles" ).listFiles( new FilenameFilter()
            {
                @Override
                public boolean accept( final File dir, final String name )
                {
                    return name.endsWith( ".jar" );
                }
            } );
            for ( final File bundle : bundles )
            {
                eclipse.startBundle( eclipse.installBundle( bundle.toURI().toASCIIString() ) );
            }
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private Map<String, String> initParams( final File pluginDir )
    {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put( "org.eclipse.equinox.simpleconfigurator.exclusiveInstallation", "false" );
        initParams.put( "osgi.java.profile.bootdelegation", "none" );
        final String bridgedPackages = scanBridgedPackages( new File( pluginDir, "p2-runtime/bridge" ) );
        if ( StringUtils.isNotBlank( bridgedPackages ) )
        {
            initParams.put( "org.osgi.framework.system.packages.extra", bridgedPackages );
        }
        initParams.put( "osgi.debug", new File( pluginDir, "p2-runtime/eclipse/.options" ).getAbsolutePath() );
        return initParams;
    }

    private String scanBridgedPackages( final File dir )
    {
        final StringBuilder bridgedPackages = new StringBuilder();
        final File[] manifests = dir.listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( final File dir, final String name )
            {
                return name.endsWith( ".manifest" );
            }
        } );
        for ( final File manifestFile : manifests )
        {
            InputStream is = null;
            try
            {
                is = new FileInputStream( manifestFile );
                final Manifest manifest = new Manifest( is );
                final String pkg = manifest.getMainAttributes().getValue( "Export-Package" );
                if ( StringUtils.isNotBlank( pkg ) )
                {
                    if ( bridgedPackages.length() > 0 )
                    {
                        bridgedPackages.append( "," );
                    }
                    bridgedPackages.append( pkg );
                }
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e );
            }
            finally
            {
                IOUtil.close( is );
            }
        }
        return bridgedPackages.toString();
    }

    private File getP2RepositoryPluginDir()
    {
        try
        {
            final GAVCoordinate pluginGav = getP2RepositoryPluginGAV();
            final PluginRepositoryArtifact pluginArtifact = pluginRepositoryManager.resolveArtifact( pluginGav );
            return pluginArtifact.getFile().getParentFile();
        }
        catch ( final NoSuchPluginRepositoryArtifactException e )
        {
            throw new IllegalStateException( "Could not locate nexus-p2-bridge-plugin", e );
        }
    }

    private GAVCoordinate getP2RepositoryPluginGAV()
    {
        final Properties props = new Properties();

        final InputStream is =
            P2Runtime.class.getResourceAsStream( "/META-INF/maven/org.sonatype.nexus.plugins/nexus-p2-bridge-plugin/pom.properties" );

        if ( is != null )
        {
            try
            {
                props.load( is );
            }
            catch ( final IOException e )
            {
                throw new RuntimeException( e.getMessage(), e );
            }
        }

        final GAVCoordinate coordinate =
            new GAVCoordinate( "org.sonatype.nexus.plugins", "nexus-p2-bridge-plugin", props.getProperty( "version" ) );

        return coordinate;
    }

}

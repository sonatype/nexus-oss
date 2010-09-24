package org.sonatype.plexus.components.ehcache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 */
@Component( role = PlexusEhCacheWrapper.class )
public class DefaultEhCacheWrapper
    implements PlexusEhCacheWrapper, Startable
{
    @Requirement
    private Logger logger;

    @Requirement
    private PlexusContainer plexusContainer;

    /** The eh cache manager. */
    private CacheManager ehCacheManager;

    protected Logger getLogger()
    {
        return logger;
    }

    public synchronized CacheManager getEhCacheManager()
    {
        if ( ehCacheManager == null )
        {
            try
            {
                constructEhCacheManager();
            }
            catch ( InterpolationException e )
            {
                getLogger().error( "Failed to initialize EHCache: " + e.getMessage(), e );

                throw new IllegalStateException( "Failed to initialize EHCache manager!", e );
            }
        }

        return ehCacheManager;
    }

    public void start()
        throws StartingException
    {
        try
        {
            // just "fetch" it to force it's creation
            getEhCacheManager();
        }
        catch ( IllegalStateException e )
        {
            throw new StartingException( e.getMessage(), e );
        }
    }

    public void stop()
        throws StoppingException
    {
        if ( ehCacheManager != null )
        {
            getLogger().info( "Shutting down EHCache manager." );

            ehCacheManager.removalAll();

            ehCacheManager.shutdown();

            ehCacheManager = null;
        }
    }

    // ==

    private void constructEhCacheManager()
        throws InterpolationException
    {
        InputStream configStream = getClass().getResourceAsStream( "/ehcache.xml" );

        if ( configStream != null )
        {
            Configuration ehConfig =
                ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream,
                    plexusContainer.getContext().getContextData() ) );

            configureDiskStore( ehConfig );

            getLogger().info(
                "Creating and configuring EHCache manager with classpath:/ehcache.xml, using disk store '"
                    + ( ehConfig.getDiskStoreConfiguration() == null ? "none"
                        : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

            ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
        }
        else
        {
            configStream = getClass().getResourceAsStream( "/ehcache-default.xml" );

            if ( configStream != null )
            {
                getLogger().info(
                    "No user EHCache configuration found, creating EHCache manager and configuring it with classpath:/ehcache-default.xml." );

                Configuration ehConfig =
                    ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream,
                        plexusContainer.getContext().getContextData() ) );

                configureDiskStore( ehConfig );

                getLogger().info(
                    "Creating and configuring EHCache manager with Nexus Default EHCache Configuration, using disk store '"
                        + ( ehConfig.getDiskStoreConfiguration() == null ? "none"
                            : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

                ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
            }
            else
            {
                getLogger().warn(
                    "Creating 'default' EHCache manager since no user or default ehcache.xml configuration found on classpath root." );

                ehCacheManager = new net.sf.ehcache.CacheManager();
            }
        }
    }

    private void configureDiskStore( Configuration ehConfig )
        throws InterpolationException
    {
        // if disk store isn't configured, don't muck with it
        if ( ehConfig.getDiskStoreConfiguration() != null && ehConfig.getDiskStoreConfiguration().getPath() != null )
        {
            // add plexus awareness with interpolation
            String path = ehConfig.getDiskStoreConfiguration().getPath();

            final RegexBasedInterpolator regexBasedInterpolator = new RegexBasedInterpolator();

            regexBasedInterpolator.addValueSource( new MapBasedValueSource(
                plexusContainer.getContext().getContextData() ) );

            try
            {
                path = regexBasedInterpolator.interpolate( path, "" );

                path = new File( path ).getCanonicalPath();
            }
            catch ( IOException e )
            {
                getLogger().warn( "Could not canonize the path '" + path + "'!", e );
            }

            ehConfig.getDiskStoreConfiguration().setPath( path );
        }
    }
}

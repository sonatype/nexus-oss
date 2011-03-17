package org.sonatype.plexus.components.ehcache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.slf4j.Logger;
import org.sonatype.inject.Parameters;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 */
@Named
public class DefaultEhCacheWrapper
    implements PlexusEhCacheWrapper
{
    private Logger logger;

    /** The eh cache manager. */
    private CacheManager ehCacheManager;

    private Map<String, String> context;

    @Inject
    public DefaultEhCacheWrapper( @Parameters Map<String, String> context, Logger logger )
    {
        this.logger = logger;
        this.context = context;
        getEhCacheManager();
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
                logger.error( "Failed to initialize EHCache: " + e.getMessage(), e );

                throw new IllegalStateException( "Failed to initialize EHCache manager!", e );
            }
        }

        return ehCacheManager;
    }

    public void stop()
    {
        if ( ehCacheManager != null )
        {
            logger.info( "Shutting down EHCache manager." );

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
                ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream, context ) );

            configureDiskStore( ehConfig );

            logger.info( "Creating and configuring EHCache manager with classpath:/ehcache.xml, using disk store '"
                + ( ehConfig.getDiskStoreConfiguration() == null ? "none"
                                : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

            ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
        }
        else
        {
            configStream = getClass().getResourceAsStream( "/ehcache-default.xml" );

            if ( configStream != null )
            {
                logger.info( "No user EHCache configuration found, creating EHCache manager and configuring it with classpath:/ehcache-default.xml." );

                Configuration ehConfig =
                    ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream, context ) );

                configureDiskStore( ehConfig );

                logger.info( "Creating and configuring EHCache manager with Nexus Default EHCache Configuration, using disk store '"
                    + ( ehConfig.getDiskStoreConfiguration() == null ? "none"
                                    : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

                ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
            }
            else
            {
                logger.warn( "Creating 'default' EHCache manager since no user or default ehcache.xml configuration found on classpath root." );

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

            regexBasedInterpolator.addValueSource( new MapBasedValueSource( context ) );

            try
            {
                path = regexBasedInterpolator.interpolate( path, "" );

                path = new File( path ).getCanonicalPath();
            }
            catch ( IOException e )
            {
                logger.warn( "Could not canonize the path '" + path + "'!", e );
            }

            ehConfig.getDiskStoreConfiguration().setPath( path );
        }
    }
}

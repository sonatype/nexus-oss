package org.sonatype.sisu.ehcache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.appcontext.AppContext;
import org.sonatype.inject.Nullable;

@Named
@Singleton
public class CacheManagerComponentImpl
    implements CacheManagerComponent
{
    private final Logger logger;

    private final AppContext appContext;

    private final CacheManager cacheManager;

    /**
     * Creates CacheManagerProviderImpl using the provided AppContext. See
     * {@link #CacheManagerProviderImpl(Logger, AppContext, File)} for details.
     * 
     * @param appContext
     * @throws IOException
     */
    @Inject
    public CacheManagerComponentImpl( @Nullable final AppContext appContext )
        throws IOException
    {
        this( appContext, null );
    }

    /**
     * Creates CacheManagerProviderImpl instance using default logger. See
     * {@link #CacheManagerProviderImpl(Logger, AppContext, File)} for details.
     * 
     * @param appContext
     * @param file
     * @throws IOException
     */
    public CacheManagerComponentImpl( final AppContext appContext, final File file )
        throws IOException
    {
        this( LoggerFactory.getLogger( CacheManagerComponentImpl.class ), appContext, file );
    }

    /**
     * Creates a new instance of CacheManagerProviderImpl by using provided logger, appcontext and file to pointing at
     * EHCache XML configuration file.
     * 
     * @param logger the logger to use, might not be {@code null}.
     * @param appContext the appContext to use, might be {@code null}.
     * @param file the EHCache XML configuration file, {@code null} if you rely on defaults.
     * @throws IOException in case of some fatal problem.
     * @throws NullPointerException if logger is null.
     */
    public CacheManagerComponentImpl( final Logger logger, final AppContext appContext, final File file )
        throws IOException, NullPointerException
    {
        if ( logger == null )
        {
            throw new NullPointerException( "Supplied logger cannot be null!" );
        }
        this.logger = logger;
        this.appContext = appContext;
        this.cacheManager = buildCacheManager( file );
    }

    public synchronized void shutdown()
    {
        getLogger().info( "Shutting down EHCache CacheManager." );
        cacheManager.removalAll();
        cacheManager.shutdown();
    }

    public CacheManager getCacheManager()
    {
        return cacheManager;
    }

    public CacheManager buildCacheManager( final File file )
        throws IOException
    {
        return buildCacheManager( getAppContext(), file );
    }

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    protected AppContext getAppContext()
    {
        return appContext;
    }

    protected CacheManager buildCacheManager( final AppContext appContext, final File file )
        throws IOException
    {
        String ehCacheXml;
        if ( file != null )
        {
            getLogger().info( "Configuring EHCache CacheManager from file \"{}\".", file.getAbsolutePath() );
            ehCacheXml = locateCacheManagerConfigurationFromFile( file );
        }
        else
        {
            getLogger().info( "Configuring EHCache CacheManager from classpath." );
            ehCacheXml = locateCacheManagerConfigurationFromClasspath();
        }

        Configuration configuration;
        if ( appContext != null )
        {
            configuration =
                ConfigurationFactory.parseConfiguration( new ByteArrayInputStream(
                    appContext.interpolate( ehCacheXml ).getBytes( "UTF-8" ) ) );
        }
        else
        {
            configuration =
                ConfigurationFactory.parseConfiguration( new ByteArrayInputStream( ehCacheXml.getBytes( "UTF-8" ) ) );
        }

        configureDiskStore( appContext, configuration );

        return new CacheManager( configuration );
    }

    protected String locateCacheManagerConfigurationFromFile( final File file )
        throws IOException
    {
        final FileInputStream fis = new FileInputStream( file );
        try
        {
            return IO.toString( fis );
        }
        finally
        {
            fis.close();
        }
    }

    protected String locateCacheManagerConfigurationFromClasspath()
        throws IOException
    {
        InputStream configStream = getClass().getResourceAsStream( "/ehcache.xml" );

        if ( configStream != null )
        {
            getLogger().debug( "Using configuration found at classpath:/ehcache.xml" );
            return IO.toString( configStream );
        }
        else
        {
            configStream = getClass().getResourceAsStream( "/ehcache-default.xml" );

            if ( configStream != null )
            {
                getLogger().debug( "Using configuration found at classpath:/ehcache-default.xml" );
                return IO.toString( configStream );
            }
            else
            {
                logger.info( "No EHCache configuration found an classpath, using defaults (is this really what you want?)." );
                return "";
            }
        }
    }

    protected void configureDiskStore( final AppContext appContext, final Configuration ehConfig )
    {
        if ( ehConfig.getDiskStoreConfiguration() != null && ehConfig.getDiskStoreConfiguration().getPath() != null )
        {
            final String path = ehConfig.getDiskStoreConfiguration().getPath();
            final String interpolatedPath = appContext != null ? appContext.interpolate( path ) : path;
            try
            {
                ehConfig.getDiskStoreConfiguration().setPath( new File( interpolatedPath ).getCanonicalPath() );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Could not canonize the path \"{}\"!", interpolatedPath, e );
            }
        }
    }
}

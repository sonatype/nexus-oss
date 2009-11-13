package org.sonatype.plexus.components.ehcache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

/**
 * The Class EhCacheCacheManager is a thin wrapper around EhCache, just to make things going.
 */
@Component( role = PlexusEhCacheWrapper.class )
public class DefaultEhCacheWrapper
    extends AbstractLogEnabled
    implements PlexusEhCacheWrapper, Contextualizable, Disposable
{

    /**
     * The application interpolation service.
     */
    private RegexBasedInterpolator regexBasedInterpolator = new RegexBasedInterpolator();

    /** The eh cache manager. */
    private net.sf.ehcache.CacheManager ehCacheManager;

    private Map<?, ?> variables;

    public net.sf.ehcache.CacheManager getEhCacheManager()
    {
        if ( this.ehCacheManager == null )
        {
            try
            {
                this.constructEhCacheManager();
            }
            catch ( InterpolationException e )
            {
                this.getLogger().error( "Failed to initialize EHCache: " + e.getMessage(), e );
            }
        }

        return this.ehCacheManager;
    }

    public void start()
        throws StartingException
    {
        if ( this.ehCacheManager == null )
        {
            try
            {
                constructEhCacheManager();
            }
            catch ( InterpolationException e )
            {
                throw new StartingException( "Could not start " + this.getClass().getSimpleName() + ":", e );
            }
        }
        else
        {
            this.getLogger().debug( "DefaultEhCacheManager is already started." );
        }

    }

    public void stop()
        throws StoppingException
    {
        dispose();
    }

    private void constructEhCacheManager()
        throws InterpolationException
    {
        InputStream configStream = this.getClass().getResourceAsStream( "/ehcache.xml" );

        if ( configStream != null )
        {
            Configuration ehConfig =
                ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream, variables ) );

            configureDiskStore( ehConfig );

            getLogger().info(
                              "Creating and configuring EHCache manager with classpath:/ehcache.xml, using disk store '"
                                  + ( ehConfig.getDiskStoreConfiguration() == null ? "none" : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

            ehCacheManager = new net.sf.ehcache.CacheManager( ehConfig );
        }
        else
        {
            configStream = this.getClass().getResourceAsStream( "/ehcache-default.xml" );

            if ( configStream != null )
            {
                getLogger().info(
                                  "No user EHCache configuration found, creating EHCache manager and configuring it with classpath:/ehcache-default.xml." );

                Configuration ehConfig =
                    ConfigurationFactory.parseConfiguration( new InterpolatingInputStream( configStream, variables ) );

                configureDiskStore( ehConfig );

                getLogger().info(
                                  "Creating and configuring EHCache manager with Nexus Default EHCache Configuration, using disk store '"
                                      + ( ehConfig.getDiskStoreConfiguration() == null ? "none" : ehConfig.getDiskStoreConfiguration().getPath() ) + "'" );

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
        //if disk store isn't configured, don't muck with it
        if ( ehConfig.getDiskStoreConfiguration() != null
            && ehConfig.getDiskStoreConfiguration().getPath() != null )
        {
            // add plexus awareness with interpolation
            String path = ehConfig.getDiskStoreConfiguration().getPath();
    
            try
            {
                path = this.regexBasedInterpolator.interpolate( path, "" );
    
                path = new File( path ).getCanonicalPath();
            }
            catch ( IOException e )
            {
                getLogger().warn( "Could not canonize the path '" + path + "'!", e );
            }
    
            ehConfig.getDiskStoreConfiguration().setPath( path );
        }
    }

    public void contextualize( Context context )
        throws ContextException
    {
        Map<Object, Object> vars = new HashMap<Object, Object>();

        vars.putAll( context.getContextData() );
        // FIXME: bad, everything should come from Plexus context
        vars.putAll( System.getenv() );
        // FIXME: bad, everything should come from Plexus context
        vars.putAll( System.getProperties() );

        this.variables = vars;
    }

    public void dispose()
    {
        if ( this.ehCacheManager != null )
        {
            getLogger().info( "Shutting down EHCache manager." );

            ehCacheManager.removalAll();

            ehCacheManager.shutdown();

            ehCacheManager = null;
        }
    }

}

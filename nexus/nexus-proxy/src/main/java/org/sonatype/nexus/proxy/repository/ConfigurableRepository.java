package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.mirror.DefaultPublishedMirrors;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;

public class ConfigurableRepository
    extends AbstractConfigurable
{
    private PublishedMirrors pMirrors;
    
    public static final String CONFIG_LOCAL_URL = "localUrl";

    @Override
    protected CRepository getCurrentConfiguration( boolean forWrite )
    {
        return ( (CRepositoryCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return null;
    }

    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return null;
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof CRepository )
        {
            return new CRepositoryCoreConfiguration( getApplicationConfiguration(), (CRepository) configuration,
                                                     getExternalConfigurationHolderFactory() );
        }
        else if ( configuration instanceof CRepositoryCoreConfiguration )
        {
            return (CRepositoryCoreConfiguration) configuration;
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                + configuration.getClass().getName() + "\" and not the required \"" + CRepository.class.getName()
                + "\"!" );
        }
    }

    public String getId()
    {
        return getCurrentConfiguration( false ).getId();
    }

    public void setId( String id )
    {
        getCurrentConfiguration( true ).setId( id );
    }

    public String getName()
    {
        return getCurrentConfiguration( false ).getName();
    }

    public void setName( String name )
    {
        getCurrentConfiguration( true ).setName( name );
    }

    public String getPathPrefix()
    {
        // a "fallback" mechanism: id's must be unique now across nexus,
        // but some older systems may have groups/reposes with same ID. To clear out the ID-clash, we will need to
        // change IDs, but we must _not_ change the published URLs on those systems.
        String pathPrefix = getCurrentConfiguration( false ).getPathPrefix();

        if ( !StringUtils.isBlank( pathPrefix ) )
        {
            return pathPrefix;
        }
        else
        {
            return getId();
        }
    }

    public void setPathPrefix( String prefix )
    {
        getCurrentConfiguration( true ).setPathPrefix( prefix );
    }

    public boolean isIndexable()
    {
        return getCurrentConfiguration( false ).isIndexable();
    }

    public void setIndexable( boolean indexable )
    {
        getCurrentConfiguration( true ).setIndexable( indexable );
    }

    public String getLocalUrl()
    {
        // see NEXUS-2482
        String localUrl = getCurrentConfiguration( false ).getLocalStorage().getUrl();
        
        return ( StringUtils.isEmpty( localUrl ) ) ? getCurrentConfiguration( false ).defaultLocalStorageUrl : localUrl;
    }

    public void setLocalUrl( String localUrl )
        throws StorageException
    {
        String oldLocalUrl = this.getLocalUrl();

        String newLocalUrl = localUrl.trim();

        if ( newLocalUrl.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            newLocalUrl = newLocalUrl.substring( 0, newLocalUrl.length() - 1 );
        }
        
        if ( !oldLocalUrl.equals( newLocalUrl ) )
        {
            getConfigurationChanges().put( CONFIG_LOCAL_URL, newLocalUrl );
        }

        getCurrentConfiguration( true ).getLocalStorage().setUrl( newLocalUrl );
    }

    public LocalStatus getLocalStatus()
    {
        if( getCurrentConfiguration( false ).getLocalStatus() == null )
        {
            return null;
        }
        return LocalStatus.valueOf( getCurrentConfiguration( false ).getLocalStatus() );
    }

    public void setLocalStatus( LocalStatus localStatus )
    {
        getCurrentConfiguration( true ).setLocalStatus( localStatus.toString() );
    }

    public RepositoryWritePolicy getWritePolicy()
    {
        return RepositoryWritePolicy.valueOf( getCurrentConfiguration( false ).getWritePolicy() );
    }

    public void setWritePolicy( RepositoryWritePolicy writePolicy )
    {
        getCurrentConfiguration( true ).setWritePolicy( writePolicy.name() );
    }    
    
    public boolean isBrowseable()
    {
        return getCurrentConfiguration( false ).isBrowseable();
    }

    public void setBrowseable( boolean browseable )
    {
        getCurrentConfiguration( true ).setBrowseable( browseable );
    }

    public boolean isUserManaged()
    {
        return getCurrentConfiguration( false ).isUserManaged();
    }

    public void setUserManaged( boolean userManaged )
    {
        getCurrentConfiguration( true ).setUserManaged( userManaged );
    }

    public boolean isExposed()
    {
        return getCurrentConfiguration( false ).isExposed();
    }

    public void setExposed( boolean exposed )
    {
        getCurrentConfiguration( true ).setExposed( exposed );
    }

    public int getNotFoundCacheTimeToLive()
    {
        return getCurrentConfiguration( false ).getNotFoundCacheTTL();
    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        getCurrentConfiguration( true ).setNotFoundCacheTTL( notFoundCacheTimeToLive );
    }

    public boolean isNotFoundCacheActive()
    {
        return getCurrentConfiguration( false ).isNotFoundCacheActive();
    }

    public void setNotFoundCacheActive( boolean notFoundCacheActive )
    {
        getCurrentConfiguration( true ).setNotFoundCacheActive( notFoundCacheActive );
    }

    public PublishedMirrors getPublishedMirrors()
    {
        if ( pMirrors == null )
        {
            pMirrors = new DefaultPublishedMirrors( (CRepositoryCoreConfiguration) getCurrentCoreConfiguration() );
        }

        return pMirrors;
    }
}

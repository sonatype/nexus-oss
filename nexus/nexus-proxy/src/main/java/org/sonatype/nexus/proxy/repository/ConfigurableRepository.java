/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
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
    protected CRepositoryCoreConfiguration wrapConfiguration( Object configuration )
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

    public String getProviderRole()
    {
        return getCurrentConfiguration( false ).getProviderRole();
    }

    public String getProviderHint()
    {
        return getCurrentConfiguration( false ).getProviderHint();
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

    public boolean isSearchable()
    {
        return getCurrentConfiguration( false ).isSearchable();
    }

    public void setSearchable( boolean searchable )
    {
        getCurrentConfiguration( true ).setSearchable( searchable );
    }

    public String getLocalUrl()
    {
        // see NEXUS-2482
        if ( getCurrentConfiguration( false ).getLocalStorage() == null 
            || StringUtils.isEmpty( getCurrentConfiguration( false ).getLocalStorage().getUrl() ) )
        {
            return getCurrentConfiguration( false ).defaultLocalStorageUrl; 
        }
        
        return getCurrentConfiguration( false ).getLocalStorage().getUrl();
    }

    public void setLocalUrl( String localUrl )
        throws StorageException
    {
        String newLocalUrl = null;
        
        if ( !StringUtils.isEmpty( localUrl ) )
        {
            newLocalUrl = localUrl.trim();
        }
        
        if ( newLocalUrl != null 
            && newLocalUrl.endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            newLocalUrl = newLocalUrl.substring( 0, newLocalUrl.length() - 1 );
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

/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This class is a base abstract class for remot storages.
 * 
 * @author cstamas
 */
public abstract class AbstractRemoteRepositoryStorage
    extends LoggingComponent
    implements RemoteRepositoryStorage
{

    /**
     * Since storages are shared, we are tracking the last changes from each of them.
     */
    private Map<String, Long> repositoryContexts = new HashMap<String, Long>();

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    public URL getAbsoluteUrlFromBase( RepositoryItemUid uid )
        throws StorageException
    {
        StringBuffer urlStr = new StringBuffer( uid.getRepository().getRemoteUrl() );

        if ( uid.getPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( uid.getPath() );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( uid.getPath() );
        }

        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            throw new StorageException( "The repository has broken URL!", e );
        }
    }

    /**
     * Remote storage specific, when the remote connection settings are actually applied.
     * 
     * @param context
     */
    protected abstract void updateContext( RemoteStorageContext context );

    protected synchronized RemoteStorageContext getRemoteStorageContext( Repository repository )
    {
        if ( repository.getRemoteStorageContext() != null )
        {
            // we have repo specific settings
            if ( repositoryContexts.containsKey( repository.getId() ) )
            {
                if ( repository.getRemoteStorageContext().getLastChanged() > repositoryContexts
                    .get( repository.getId() ).longValue() )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Remote storage settings change detected, updating..." );
                    }

                    updateContext( repository.getRemoteStorageContext() );

                    repositoryContexts.put( repository.getId(), Long.valueOf( repository
                        .getRemoteStorageContext().getLastChanged() ) );
                }
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Remote storage settings change detected, updating..." );
                }

                updateContext( repository.getRemoteStorageContext() );

                repositoryContexts.put( repository.getId(), Long.valueOf( repository
                    .getRemoteStorageContext().getLastChanged() ) );
            }

        }

        return repository.getRemoteStorageContext();
    }

    public boolean isReachable( Repository repository )
        throws StorageException
    {
        return containsItem( repository.createUid( RepositoryItemUid.PATH_ROOT ) );
    }

    public boolean containsItem( RepositoryItemUid uid )
        throws StorageException
    {
        return containsItem( uid, 0 );
    }

    // helper methods

    protected CRemoteConnectionSettings getRemoteConnectionSettings( RemoteStorageContext ctx )
    {
        return (CRemoteConnectionSettings) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS );
    }

    protected CRemoteAuthentication getRemoteAuthenticationSettings( RemoteStorageContext ctx )
    {
        return (CRemoteAuthentication) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS );
    }

    protected CRemoteHttpProxySettings getRemoteHttpProxySettings( RemoteStorageContext ctx )
    {
        return (CRemoteHttpProxySettings) ctx
            .getRemoteConnectionContextObject( RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS );
    }

}

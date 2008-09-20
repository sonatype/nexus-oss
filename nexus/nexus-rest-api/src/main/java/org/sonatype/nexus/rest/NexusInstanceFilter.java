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
package org.sonatype.nexus.rest;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.mgt.SecurityManager;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jsecurity.realms.PlexusSecurity;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.plexus.rest.PlexusRestletUtils;

/**
 * A restlet Filter, that handles "instanceName" attribute to put the correspondent (local or remote/proxied) Nexus
 * instance into request attributes, hance making local and remote call transparent in underlying restlets.
 * 
 * @author cstamas
 */
public class NexusInstanceFilter
    extends Filter
{

    /** Key to store nexus instance, */
    public static final String NEXUS_INSTANCE_KEY = "instanceName";

    /**
     * The filter constructor.
     * 
     * @param context
     */
    public NexusInstanceFilter( Context context )
    {
        super( context );
    }

    /**
     * A beforeHandle will simply embed in request attributes a Nexus interface implemntor, depending on key used to
     * name it.
     */
    protected int beforeHandle( Request request, Response response )
    {
        String instanceName = request.getAttributes().containsKey( NEXUS_INSTANCE_KEY ) ? (String) request
            .getAttributes().get( NEXUS_INSTANCE_KEY ) : "local";

        Nexus nexus = null;

        if ( "local".equals( instanceName ) )
        {
            nexus = getLocalNexus();
        }
        else
        {
            nexus = getRemoteNexus( instanceName );
        }

        request.getAttributes().put( Nexus.ROLE, nexus );
        
        request.getAttributes().put( NexusSecurity.ROLE, getNexusSecurity() );

        request.getAttributes().put( SecurityManager.class.getName(), getSecurityManager() );
        
        return CONTINUE;
    }
    
    protected NexusSecurity getNexusSecurity()
    {
        try
        {
            return ( NexusSecurity ) PlexusRestletUtils.plexusLookup(
                getContext(),
                NexusSecurity.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup NexusSecurity!", e );
        }
    }

    protected SecurityManager getSecurityManager()
    {
        try
        {
            return (SecurityManager) PlexusRestletUtils.plexusLookup(
                getContext(),
                PlexusSecurity.class.getName() );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup PlexusSecurity!", e );
        }
    }

    /**
     * Getting local nexus instance, that is local, hence runs within this Plexus container.
     * 
     * @return
     */
    protected Nexus getLocalNexus()
    {
        try
        {
            return (Nexus) PlexusRestletUtils.plexusLookup( getContext(), Nexus.ROLE );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Cannot lookup local Nexus!", e );
        }
    }

    /**
     * Getting remote nexus instance.
     * 
     * @param name
     * @return
     */
    protected Nexus getRemoteNexus( String alias )
    {
        try
        {
            Nexus local = getLocalNexus();

            CRemoteNexusInstance remoteInstance = local.readRemoteNexusInstance( alias );

            if ( remoteInstance == null )
            {
                throw new IllegalArgumentException( "Nexus instance with alias [" + alias + "] is unknown!" );
            }
            else
            {
                throw new IllegalArgumentException( "Nexus remoting is not implemented!" );
            }
        }
        catch ( IOException e )
        {
            throw new IllegalArgumentException( "Nexus instance with alias [" + alias + "] cannot be created!", e );
        }
    }

}

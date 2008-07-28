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
package org.sonatype.nexus.rest.privileges;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;

public class PrivilegeListResourceHandler
extends AbstractPrivilegeResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public PrivilegeListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs/
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Repositories by getting the from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        PrivilegeListResourceResponse response = new PrivilegeListResourceResponse();
        
        Collection<CApplicationPrivilege> appPrivs = getNexusSecurityConfiguration().listApplicationPrivileges();
        
        for ( CApplicationPrivilege priv : appPrivs )
        {
            PrivilegeBaseStatusResource res = nexusToRestModel( priv );
            
            if ( res != null )
            {
                response.addData( res );
            }
        }
        
        Collection<CRepoTargetPrivilege> tarPrivs = getNexusSecurityConfiguration().listRepoTargetPrivileges();
        
        for ( CRepoTargetPrivilege priv : tarPrivs )
        {
            PrivilegeBaseStatusResource res = nexusToRestModel( priv );
            
            if ( res != null )
            {
                response.addData( res );
            }
        }
                
        return serialize( variant, response );
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation representation )
    {
        PrivilegeResourceRequest request = (PrivilegeResourceRequest) deserialize( new PrivilegeResourceRequest() );

        if ( request == null )
        {
            return;
        }
        else
        {
            PrivilegeListResourceResponse response = new PrivilegeListResourceResponse();
            
            PrivilegeBaseResource resource = request.getData();
            
            List<String> methods = resource.getMethod();
            
            try
            {
                // Add a new privilege for each method
                for ( String method : methods )
                {
                    // Currently can only add new target types, application types are hardcoded
                    if ( PrivilegeTargetResource.class.isAssignableFrom( resource.getClass() ) )
                    {
                        PrivilegeTargetResource res = ( PrivilegeTargetResource ) resource;
                        
                        CRepoTargetPrivilege priv = new CRepoTargetPrivilege();
                        priv.setMethod( method );
                        priv.setName( res.getName() + " - (" + method + ")");
                        priv.setRepositoryTargetId( res.getRepositoryTargetId() );
                        priv.setRepositoryId( res.getRepositoryId() );
                        priv.setGroupId( res.getRepositoryGroupId() );
                        
                        if ( getNexus().readRepositoryTarget( res.getRepositoryTargetId() ) == null )
                        {
                            getResponse().setEntity( serialize( representation, getNexusErrorResponse( "repositoryTargetId", "Selected Repository Target does not exist." ) ) );
                            
                            return;
                        }
                        
                        // Validate repo exists
                        if ( !StringUtils.isEmpty( res.getRepositoryId() ) )
                        {
                            getNexus().readRepository( res.getRepositoryId() );
                        }
                        
                        // Validate group exists
                        if ( !StringUtils.isEmpty( res.getRepositoryGroupId() ) )
                        {
                            getNexus().readRepositoryGroup( res.getRepositoryGroupId() );
                        }
                        
                        getNexusSecurityConfiguration().createRepoTargetPrivilege( priv );
                        
                        response.addData( nexusToRestModel( priv ) );
                    }
                }
                
                getResponse().setEntity( serialize( representation, response ) );
            }
            catch ( ConfigurationException e )
            {                
                handleConfigurationException( e, representation );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
            catch ( NoSuchRepositoryException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );
                getResponse().setEntity( serialize( representation, getNexusErrorResponse( "repositoryId", e.getMessage() ) ) );
            }
            catch ( NoSuchRepositoryGroupException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );
                getResponse().setEntity( serialize( representation, getNexusErrorResponse( "repositoryGroupId", e.getMessage() ) ) );
            }
        }
    }
}

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

import java.util.Collection;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.InvalidConfigurationException;
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

        Collection<CPrivilege> privs = getNexusSecurity().listPrivileges();

        for ( CPrivilege priv : privs )
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

            // currently we are allowing only of repotarget privs, so enforcing checkfor it
            if ( !TYPE_REPO_TARGET.equals( resource.getType() ) )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );
                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse( "type", "Not allowed privilege type!" ) ) );
            }

            List<String> methods = resource.getMethod();

            if ( methods == null || methods.size() == 0 )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );
                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "method",
                        "No method(s) supplied, must select at least one method." ) ) );
            }
            else
            {
                try
                {
                    boolean success = true;
                    // Add a new privilege for each method
                    for ( String method : methods )
                    {
                        // Currently can only add new target types, application types are hardcoded
                        if ( PrivilegeTargetResource.class.isAssignableFrom( resource.getClass() ) )
                        {
                            PrivilegeTargetResource res = (PrivilegeTargetResource) resource;
    
                            CPrivilege priv = new CPrivilege();
                            
                            priv.setName( res.getName() != null ? res.getName() + " - (" + method + ")" : null );
                            priv.setDescription( res.getDescription() );
                            priv.setType( "target" );
                            
                            CProperty prop = new CProperty();
                            prop.setKey( "method" );
                            prop.setValue( method );
                            
                            priv.addProperty( prop );
                            
                            prop = new CProperty();
                            prop.setKey( "repositoryTargetId" );
                            prop.setValue( res.getRepositoryTargetId() );
                            
                            priv.addProperty( prop );
                            
                            prop = new CProperty();
                            prop.setKey( "repositoryId" );
                            prop.setValue( res.getRepositoryId() );
                            
                            priv.addProperty( prop );
                            
                            prop = new CProperty();
                            prop.setKey( "repositoryGroupId" );
                            prop.setValue( res.getRepositoryGroupId() );
                            
                            priv.addProperty( prop );
    
                            getNexusSecurity().createPrivilege( priv );
    
                            response.addData( nexusToRestModel( priv ) );
                        }
                        else
                        {
                            success = false;
                            getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );
                            getResponse().setEntity(
                                serialize( representation, getNexusErrorResponse(
                                    "type",
                                    "An invalid type was entered." ) ) );
                            break;
                        }
                    }
                    
                    if ( success )
                    {
                        getResponse().setEntity( serialize( representation, response ) );
                        
                        getResponse().setStatus( Status.SUCCESS_CREATED );
                    }
                }
                catch ( InvalidConfigurationException e )
                {
                    handleInvalidConfigurationException( e, representation );
                }
            }
        }
    }
}

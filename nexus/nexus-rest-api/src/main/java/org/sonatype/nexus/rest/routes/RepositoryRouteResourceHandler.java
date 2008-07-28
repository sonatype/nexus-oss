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
package org.sonatype.nexus.rest.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.PatternSyntaxException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;

/**
 * Resource handler for Repository route resource.
 * 
 * @author cstamas
 */
public class RepositoryRouteResourceHandler
    extends AbstractRepositoryRouteResourceHandler
{
    public static final String ROUTE_ID_KEY = "routeId";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryRouteResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected String getRouteId()
    {
        return getRequest().getAttributes().get( ROUTE_ID_KEY ).toString();
    }

    /**
     * We are handling HTTP GET's.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository route representation.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            CGroupsSettingPathMappingItem route = getNexus().readGroupsSettingPathMapping( getRouteId() );

            if ( route == null )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "No such route" );

                return null;
            }

            RepositoryRouteResource resource = new RepositoryRouteResource();

            resource.setId( getRouteId() );

            resource.setGroupId( route.getGroupId() );

            resource.setRuleType( config2resourceType( route.getRouteType() ) );

            resource.setPattern( route.getRoutePattern() );

            resource.setRepositories( getRepositoryRouteMemberRepositoryList( getRequest()
                .getResourceRef().getParentRef(), route.getRepositories() ) );

            RepositoryRouteResourceResponse response = new RepositoryRouteResourceResponse();

            response.setData( resource );

            return serialize( variant, response );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.SEVERE, "Cannot find a repository declared within a group!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
        }
        catch ( IndexOutOfBoundsException e )
        {
            getLogger().log( Level.WARNING, "Repository route not found, id=" + getRouteId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Route Not Found" );

            return null;
        }
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a repository route.
     */
    public void put( Representation representation )
    {
        RepositoryRouteResourceResponse response = (RepositoryRouteResourceResponse) deserialize( new RepositoryRouteResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            RepositoryRouteResource resource = response.getData();

            if ( ( !RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) && ( resource
                .getRepositories() == null || resource.getRepositories().size() == 0 ) )
                || resource.getId() == null || !resource.getId().equals( getRouteId() ) )
            {
                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The route cannot have zero repository members!" );

                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "repositories",
                        "The route cannot have zero repository members!" ) ) );

                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "repositories",
                        "The route cannot have zero repository members!" ) ) );

                return;
            }
            else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) )
            {
                resource.setRepositories( null );
            }

            try
            {
                CGroupsSettingPathMappingItem route = getNexus().readGroupsSettingPathMapping( getRouteId() );

                if ( route == null )
                {
                    getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Route not found!" );

                    return;
                }

                route.setId( getRouteId() );

                route.setGroupId( resource.getGroupId() );

                route.setRoutePattern( resource.getPattern() );

                route.setRouteType( resource2configType( resource.getRuleType() ) );

                List<String> repositories = new ArrayList<String>( resource.getRepositories().size() );

                for ( RepositoryRouteMemberRepository repo : (List<RepositoryRouteMemberRepository>) resource
                    .getRepositories() )
                {
                    repositories.add( repo.getId() );
                }

                route.setRepositories( repositories );

                getNexus().updateGroupsSettingPathMapping( route );
            }
            catch ( ConfigurationException e )
            {
                getLogger().log( Level.SEVERE, "Configuration error!", e );

                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );

                if ( e.getCause() != null && e.getCause() instanceof PatternSyntaxException )
                {
                    getResponse().setEntity(
                        serialize( representation, getNexusErrorResponse( "pattern", e.getMessage() ) ) );
                }
                else
                {
                    getResponse().setEntity( serialize( representation, getNexusErrorResponse( "*", e.getMessage() ) ) );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().log( Level.SEVERE, "Cannot find a repository referenced within a route!", e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot find a repository referenced within a route!" );

                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "repositories",
                        "Cannot find a repository referenced within a route!" ) ) );
            }
            catch ( IOException e )
            {
                getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                getLogger().log( Level.SEVERE, "Got IO Exception!", e );
            }
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a repository route.
     */
    public void delete()
    {
        try
        {
            CGroupsSettingPathMappingItem route = getNexus().readGroupsSettingPathMapping( getRouteId() );

            if ( route == null )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Route not found!" );

                return;
            }

            getNexus().deleteGroupsSettingPathMapping( getRouteId() );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );
        }
    }
}

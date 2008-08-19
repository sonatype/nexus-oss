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
import java.util.Collection;
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
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;

/**
 * A resource list for Repository route list.
 * 
 * @author cstamas
 */
public class RepositoryRouteListResourceHandler
    extends AbstractRepositoryRouteResourceHandler
{
    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryRouteListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Repository routes by getting them from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        RepositoryRouteListResourceResponse response = new RepositoryRouteListResourceResponse();

        Collection<CGroupsSettingPathMappingItem> mappings = getNexus().listGroupsSettingPathMapping();

        RepositoryRouteListResource resource = null;

        try
        {
            for ( CGroupsSettingPathMappingItem item : mappings )
            {
                resource = new RepositoryRouteListResource();
                
                resource.setGroupId( item.getGroupId() );

                resource.setResourceURI( calculateSubReference( item.getId() ).toString() );

                resource.setRuleType( config2resourceType( item.getRouteType() ) );

                resource.setPattern( item.getRoutePattern() );

                resource.setRepositories( getRepositoryRouteMemberRepositoryList( getRequest().getResourceRef(), item
                    .getRepositories() ) );

                response.addData( resource );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.SEVERE, "Cannot find a repository declared within a mapping!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
        }

        return serialize( variant, response );
    }

    /**
     * This service allows POST.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation entity )
    {
        RepositoryRouteResourceResponse response = (RepositoryRouteResourceResponse) deserialize( new RepositoryRouteResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            RepositoryRouteResource resource = response.getData();

            if ( !RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() )
                && ( resource.getRepositories() == null || resource.getRepositories().size() == 0 ) )
            {
                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The route cannot have zero repository members!" );

                getResponse().setEntity(
                    serialize( entity, getNexusErrorResponse(
                        "repositories",
                        "The route cannot have zero repository members!" ) ) );

                return;
            }
            else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) )
            {
                resource.setRepositories( null );
            }

            resource.setId( Long.toHexString( System.currentTimeMillis() ) );

            try
            {
                CGroupsSettingPathMappingItem route = new CGroupsSettingPathMappingItem();

                route.setId( resource.getId() );
                
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

                getNexus().createGroupsSettingPathMapping( route );
                
                resource.setGroupId( route.getGroupId() );

                getResponse().setEntity( serialize( entity, response ) );
                
                getResponse().setStatus( Status.SUCCESS_CREATED );
            }
            catch ( ConfigurationException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error." );

                if ( e.getCause() != null && e.getCause() instanceof PatternSyntaxException )
                {
                    getResponse().setEntity( serialize( entity, getNexusErrorResponse( "pattern", e.getMessage() ) ) );
                }
                else
                {
                    handleConfigurationException( e, entity );
                }
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().log( Level.SEVERE, "Cannot find a repository referenced within a route!", e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot find a repository referenced within a route!" );

                getResponse().setEntity(
                    serialize( entity, getNexusErrorResponse(
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

}

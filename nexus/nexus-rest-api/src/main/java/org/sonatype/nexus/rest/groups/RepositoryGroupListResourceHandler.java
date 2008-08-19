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
package org.sonatype.nexus.rest.groups;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.InvalidGroupingException;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;

/**
 * A resource list for RepositoryGroup list.
 * 
 * @author cstamas
 */
public class RepositoryGroupListResourceHandler
    extends AbstractRepositoryGroupResourceHandler
{

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryGroupListResourceHandler( Context context, Request request, Response response )
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
        RepositoryGroupListResourceResponse response = new RepositoryGroupListResourceResponse();

        Collection<CRepositoryGroup> groups = getNexus().listRepositoryGroups();

        try
        {
            for ( CRepositoryGroup group : groups )
            {
                RepositoryGroupListResource resource = new RepositoryGroupListResource();

                resource.setResourceURI( calculateSubReference( group.getGroupId() ).toString() );

                resource.setId( group.getGroupId() );

                resource.setFormat( getNexus().getRepositoryGroupType( group.getGroupId() ) );

                resource.setName( group.getName() );

                // just to trigger list creation, and not stay null coz of XStream serialization
                resource.getRepositories();

                for ( String repoId : (List<String>) group.getRepositories() )
                {
                    RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

                    member.setId( repoId );

                    member.setName( getNexus().getRepository( repoId ).getName() );

                    member.setResourceURI( calculateRepositoryReference( repoId ).toString() );

                    resource.addRepository( member );

                }

                response.addData( resource );

            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.SEVERE, "Cannot find a repository declared within a group!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getLogger().log( Level.SEVERE, "Cannot find a repository group!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
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
        RepositoryGroupResourceResponse response = (RepositoryGroupResourceResponse) deserialize( new RepositoryGroupResourceResponse() );

        if ( response == null )
        {
            return;
        }
        else
        {
            RepositoryGroupResource resource = response.getData();

            if ( resource.getRepositories() == null || resource.getRepositories().size() == 0 )
            {
                getLogger().log(
                    Level.INFO,
                    "The repository group with ID=" + resource.getId() + " have zero repository members!" );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The group cannot have zero repository members!" );

                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "repositories",
                        "The group cannot have zero repository members!" ) ) );

                return;
            }

            try
            {
                CRepositoryGroup group = getNexus().readRepositoryGroup( resource.getId() );

                if ( group != null )
                {
                    getLogger().log(
                        Level.INFO,
                        "The repository group with ID=" + group.getGroupId() + " already exists!" );

                    getResponse().setStatus(
                        Status.CLIENT_ERROR_BAD_REQUEST,
                        "The repository group with ID=" + group.getGroupId() + " already exists!" );

                    getResponse().setEntity(
                        serialize( representation, getNexusErrorResponse( "id", "The repository group with id="
                            + group.getGroupId() + " already exists!" ) ) );

                    return;
                }
            }
            catch ( NoSuchRepositoryGroupException ex )
            {
                CRepositoryGroup group = new CRepositoryGroup();

                group.setGroupId( resource.getId() );

                group.setName( resource.getName() );

                try
                {
                    validateGroup( resource );

                    for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) resource
                        .getRepositories() )
                    {
                        group.addRepository( member.getId() );
                    }

                    getNexus().createRepositoryGroup( group );
                    
                    getResponse().setStatus( Status.SUCCESS_CREATED );
                }
                catch ( NoSuchRepositoryException e )
                {
                    getLogger().log(
                        Level.WARNING,
                        "Repository referenced by Repository Group Not Found, GroupId=" + group.getGroupId(),
                        e );

                    getResponse().setStatus(
                        Status.CLIENT_ERROR_BAD_REQUEST,
                        "Repository referenced by Repository Group Not Found, GroupId=" + group.getGroupId() );

                    getResponse().setEntity(
                        serialize( representation, getNexusErrorResponse(
                            "repositories",
                            "Repository referenced by Repository Group Not Found" ) ) );
                }
                catch ( InvalidGroupingException e )
                {
                    getLogger().log( Level.WARNING, "Invalid grouping detected!, GroupId=" + group.getGroupId(), e );

                    getResponse().setStatus(
                        Status.CLIENT_ERROR_BAD_REQUEST,
                        "Invalid grouping requested, GroupId=" + group.getGroupId() );

                    getResponse().setEntity(
                        serialize( representation, getNexusErrorResponse(
                            "repositories",
                            "Repository referenced by Repository Group does not share same content type!" ) ) );
                }
                catch ( IOException e )
                {
                    getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

                    getLogger().log( Level.SEVERE, "Got IO Exception!", e );
                }
            }
        }
    }
}

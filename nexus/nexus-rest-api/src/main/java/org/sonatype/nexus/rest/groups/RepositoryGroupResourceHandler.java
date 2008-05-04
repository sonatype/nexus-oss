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
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;

/**
 * Resource handler for Repository resource.
 * 
 * @author cstamas
 */
public class RepositoryGroupResourceHandler
    extends AbstractRepositoryGroupResourceHandler
{
    private String groupId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public RepositoryGroupResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.groupId = getRequest().getAttributes().get( GROUP_ID_KEY ).toString();
    }

    protected String getGroupId()
    {
        return this.groupId;
    }

    /**
     * We are handling HTTP GET's
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository resource representation.
     */
    public Representation getRepresentationHandler( Variant variant )
    {
        try
        {
            CRepositoryGroup group = getNexus().readRepositoryGroup( getGroupId() );

            RepositoryGroupResource resource = new RepositoryGroupResource();

            resource.setId( group.getGroupId() );

            resource.setName( group.getName() );

            // just to trigger list creation, and not stay null coz of XStream serialization
            resource.getRepositories();

            for ( String repoId : (List<String>) group.getRepositories() )
            {
                RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();

                member.setId( repoId );

                member.setName( getNexus().getRepository( repoId ).getName() );

                // TODO: find a better way to not hard-wire the URI!
                member.setResourceURI( calculateRepositoryReference(
                    getRequest().getResourceRef().getParentRef().getParentRef(),
                    repoId ).getPath() );

                resource.addRepository( member );
            }

            RepositoryGroupResourceResponse response = new RepositoryGroupResourceResponse();

            response.setData( resource );

            return serialize( variant, response );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().log( Level.SEVERE, "Cannot find a repository declared within a group!", e );

            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            return null;
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getLogger().log( Level.WARNING, "Repository group not found, id=" + getGroupId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Group Not Found" );

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
     * Update a repository.
     */
    public void put( Representation representation )
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
                    "The repository group with ID=" + getGroupId() + " have zero repository members!" );

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
                validateGroup( resource );
                
                CRepositoryGroup group = getNexus().readRepositoryGroup( getGroupId() );

                group.setName( resource.getName() );

                group.getRepositories().clear();

                for ( RepositoryGroupMemberRepository member : (List<RepositoryGroupMemberRepository>) resource
                    .getRepositories() )
                {
                    group.addRepository( member.getId() );
                }

                getNexus().updateRepositoryGroup( group );
            }
            catch ( NoSuchRepositoryGroupException e )
            {
                getLogger().log( Level.WARNING, "Repository group not exists, GroupId=" + getGroupId(), e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_NOT_FOUND,
                    "Repository group not exists, GroupId=" + getGroupId() );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().log(
                    Level.WARNING,
                    "Repository referenced by Repository Group Not Found, GroupId=" + getGroupId(),
                    e );

                getResponse().setStatus(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Repository referenced by Repository Group Not Found" );

                getResponse().setEntity(
                    serialize( representation, getNexusErrorResponse(
                        "repositories",
                        "Repository referenced by Repository Group Not Found" ) ) );
            }
            catch ( InvalidGroupingException e )
            {
                getLogger().log( Level.WARNING, "Invalid grouping, GroupId=" + getGroupId(), e );

                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid grouping, GroupId=" + getGroupId() );

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

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a repository.
     */
    public void delete()
    {
        try
        {
            getNexus().deleteRepositoryGroup( getGroupId() );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            getLogger().log( Level.WARNING, "Repository group not found, id=" + getGroupId() );

            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Repository Group Not Found" );
        }
        catch ( IOException e )
        {
            getResponse().setStatus( Status.SERVER_ERROR_INTERNAL );

            getLogger().log( Level.SEVERE, "Got IO Exception!", e );
        }
    }

}

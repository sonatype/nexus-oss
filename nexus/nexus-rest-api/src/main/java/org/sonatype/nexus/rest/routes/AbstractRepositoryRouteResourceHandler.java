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

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;

/**
 * Abstract base class for route resource handlers.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryRouteResourceHandler
    extends AbstractNexusResourceHandler
{
    /**
     * Standard constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractRepositoryRouteResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * Creating a list of member reposes. Since this method is used in two Resource subclasses too, and those are
     * probably mapped to different bases, a listBase param is needed to generate a correct URI, from the actual
     * subclass effective mapping.
     * 
     * @param listBase
     * @param reposList
     * @return
     * @throws NoSuchRepositoryException
     */
    protected List<RepositoryRouteMemberRepository> getRepositoryRouteMemberRepositoryList( Reference listBase,
        List<String> reposList )
        throws NoSuchRepositoryException
    {
        List<RepositoryRouteMemberRepository> members = new ArrayList<RepositoryRouteMemberRepository>( reposList
            .size() );

        for ( String repoId : reposList )
        {
            RepositoryRouteMemberRepository member = new RepositoryRouteMemberRepository();

            if ( "*".equals( repoId ) )
            {
                member.setId( "*" );

                member.setName( "ALL" );

                member.setResourceURI( null );
            }
            else
            {
                member.setId( repoId );

                member.setName( getNexus().getRepository( repoId ).getName() );

                member.setResourceURI( calculateRepositoryReference( repoId ).toString() );
            }

            members.add( member );
        }

        return members;
    }

    protected String resource2configType( String type )
    {
        if ( RepositoryRouteResource.INCLUSION_RULE_TYPE.equals( type ) )
        {
            return CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE;
        }
        else if ( RepositoryRouteResource.EXCLUSION_RULE_TYPE.equals( type ) )
        {
            return CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE;
        }
        else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( type ) )
        {
            return CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE;
        }
        else
        {
            return null;
        }
    }

    protected String config2resourceType( String type )
    {
        if ( CGroupsSettingPathMappingItem.INCLUSION_RULE_TYPE.equals( type ) )
        {
            return RepositoryRouteResource.INCLUSION_RULE_TYPE;
        }
        else if ( CGroupsSettingPathMappingItem.EXCLUSION_RULE_TYPE.equals( type ) )
        {
            return RepositoryRouteResource.EXCLUSION_RULE_TYPE;
        }
        else if ( CGroupsSettingPathMappingItem.BLOCKING_RULE_TYPE.equals( type ) )
        {
            return RepositoryRouteResource.BLOCKING_RULE_TYPE;
        }
        else
        {
            return null;
        }
    }

}

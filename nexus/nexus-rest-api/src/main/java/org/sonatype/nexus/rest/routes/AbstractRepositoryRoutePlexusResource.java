/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.routes;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;

/**
 * Abstract base class for route resource handlers.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryRoutePlexusResource
    extends AbstractNexusPlexusResource
{

    public static final String ROUTE_ID_KEY = "routeId";

    /**
     * Creating a list of member reposes. Since this method is used in two Resource subclasses too, and those are
     * probably mapped to different bases, a listBase param is needed to generate a correct URI, from the actual
     * subclass effective mapping.
     * 
     * @param listBase
     * @param reposList
     * @param request
     * @return
     * @throws NoSuchRepositoryException
     * @throws ResourceException
     */
    protected List<RepositoryRouteMemberRepository> getRepositoryRouteMemberRepositoryList( Reference listBase,
        List<String> reposList, Request request )
        throws NoSuchRepositoryException,
            ResourceException
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

                member.setResourceURI( createChildReference( request, repoId ).toString() );
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

/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.jsecurity.SecurityUtils;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.RequestRoute;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * Default implementation of Nexus Authorizer, that relies onto JSecurity.
 */
@Component( role = NexusItemAuthorizer.class )
public class DefaultNexusItemAuthorizer
    extends AbstractLogEnabled
    implements NexusItemAuthorizer
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private RepositoryRouter repositoryRouter;

    public boolean authorizePath( ResourceStoreRequest request, Map<String, Object> context, Action action )
    {
        TargetSet matched = repositoryRouter.getTargetsForRequest( request );

        try
        {
            RequestRoute route = repositoryRouter.getRequestRouteForRequest( request );

            if ( route.getTargetedRepository() != null )
            {
                // if this repository is contained in any group, we need to get those targets, and tweak the TargetMatch
                matched.addTargetSet( this.getGroupsTargetSet( route.getTargetedRepository(), route
                    .getOriginalRequestPath(), context ) );
            }
        }
        catch ( ItemNotFoundException e )
        {
            // ignore it, do nothing
        }

        return authorizePath( matched, action );
    }

    public boolean authorizePath( Repository repository, String path, Map<String, Object> context, Action action )
    {
        TargetSet matched = repository.getTargetsForRequest( path, context );

        // if this repository is contained in any group, we need to get those targets, and tweak the TargetMatch
        matched.addTargetSet( this.getGroupsTargetSet( repository, path, context ) );

        return authorizePath( matched, action );
    }

    public boolean isViewable( Repository repository )
    {
        return authorizePermission( "nexus:repoview:" + repository.getId() );
    }

    public boolean authorizePermission( String permission )
    {
        return isPermitted( Collections.singletonList( permission ) );
    }

    // ===

    protected TargetSet getGroupsTargetSet( Repository repository, String path, Map<String, Object> context )
    {
        TargetSet targetSet = new TargetSet();

        for ( Repository group : getListOfGroups( repository.getId() ) )
        {
            // are the perms transitively inherited from the groups where it is member?
            // !group.isExposed()
            if ( true )
            {
                TargetSet groupMatched = group.getTargetsForRequest( path, context );

                targetSet.addTargetSet( groupMatched );
            }
        }

        return targetSet;
    }

    protected List<Repository> getListOfGroups( String repositoryId )
    {
        List<Repository> groups = new ArrayList<Repository>();

        List<String> groupIds = repoRegistry.getGroupsOfRepository( repositoryId );

        for ( String groupId : groupIds )
        {
            try
            {
                groups.add( repoRegistry.getRepository( groupId ) );
            }
            catch ( NoSuchRepositoryException e )
            {
                // ignored
            }
        }

        return groups;
    }

    protected boolean authorizePath( TargetSet matched, Action action )
    {
        // did we hit repositories at all?
        if ( matched.getMatchedRepositoryIds().size() > 0 )
        {
            // we had reposes affected, check the targets
            // make perms from TargetSet
            return isPermitted( getTargetPerms( matched, action ) );
        }
        else
        {
            // we hit no repos, it is a virtual path, allow access
            return true;
        }
    }

    protected List<String> getTargetPerms( TargetSet matched, Action action )
    {
        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId : read
        for ( TargetMatch match : matched.getMatches() )
        {
            perms
                .add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() + ":" + action );
        }

        return perms;
    }

    protected boolean isPermitted( List<String> perms )
    {
        // Get the current user
        Subject subject = SecurityUtils.getSubject();

        if ( applicationConfiguration.isSecurityEnabled() )
        {
            if ( subject != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Checking isPermitted() with perms: " + perms.toString() );
                }

                // And finally check each of the target permissions and see if the user
                // has access, all it takes is one
                for ( String perm : perms )
                {
                    if ( subject.isPermitted( perm ) )
                    {
                        return true;
                    }
                }

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Subject is authenticated, but has none of the needed permissions, rejecting." );
                }

                return false;
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Subject is not authenticated, rejecting." );
                }

                // security is enabled, but we have nobody authenticated? Fail!
                return false;
            }
        }
        else
        {
            // sec is disabled, simply say YES
            return true;
        }
    }
}

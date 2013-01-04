/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.access;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.shiro.subject.Subject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.security.SecuritySystem;

/**
 * Default implementation of Nexus Authorizer, that relies onto JSecurity.
 */
@Component( role = NexusItemAuthorizer.class )
public class DefaultNexusItemAuthorizer
    extends AbstractLoggingComponent
    implements NexusItemAuthorizer
{
    @Requirement
    private SecuritySystem securitySystem;

    @Requirement
    private RepositoryRegistry repoRegistry;

    public boolean authorizePath( final Repository repository, final ResourceStoreRequest request, final Action action )
    {
        TargetSet matched = repository.getTargetsForRequest( request );
        if ( matched == null )
        {
            matched = new TargetSet();
        }
        // if this repository is contained in any group, we need to get those targets, and tweak the TargetMatch
        matched.addTargetSet( this.getGroupsTargetSet( repository, request ) );
        return authorizePath( matched, action );
    }

    public boolean authorizePermission( final String permission )
    {
        return isPermitted( Collections.singletonList( permission ) );
    }

    // ===

    public TargetSet getGroupsTargetSet( final Repository repository, final ResourceStoreRequest request )
    {
        final TargetSet targetSet = new TargetSet();
        for ( Repository group : getListOfGroups( repository.getId() ) )
        {
            // are the perms transitively inherited from the groups where it is member?
            // !group.isExposed()
            if ( true )
            {
                final TargetSet groupMatched = group.getTargetsForRequest( request );
                targetSet.addTargetSet( groupMatched );
                // now that we have groups of groups, this needs to be a recursive check
                targetSet.addTargetSet( getGroupsTargetSet( group, request ) );
            }
        }
        return targetSet;
    }

    public boolean authorizePath( final TargetSet matched, final Action action )
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

    public boolean isViewable( final String objectType, final String objectId )
    {
        return authorizePermission( "nexus:view:" + objectType + ":" + objectId );
    }

    // ==

    protected List<Repository> getListOfGroups( final String repositoryId )
    {
        final List<Repository> groups = new ArrayList<Repository>();
        final List<String> groupIds = repoRegistry.getGroupsOfRepository( repositoryId );
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

    protected List<String> getTargetPerms( final TargetSet matched, final Action action )
    {
        final List<String> perms = new ArrayList<String>( matched.getMatches().size() );
        // nexus : 'target' + targetId : repoId : read
        for ( TargetMatch match : matched.getMatches() )
        {
            perms.add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() + ":" + action );
        }
        return perms;
    }

    protected boolean isPermitted( final List<String> perms )
    {
        if ( securitySystem.isSecurityEnabled() )
        {
            // Get the current user
            final Subject subject = securitySystem.getSubject();
            if ( subject != null ) // TODO: isn't this missing? "&& subject.isAuthenticated()"
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
                        // TODO: we should remember/cache these decisions per-thread
                        // and not re-evaluate it always from Security
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
                // security is enabled, but we have nobody authenticated? Fail!
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Subject is not authenticated, rejecting." );
                }
                return false;
            }
        }
        else
        {
            // security is disabled, simply say YES
            return true;
        }
    }
}

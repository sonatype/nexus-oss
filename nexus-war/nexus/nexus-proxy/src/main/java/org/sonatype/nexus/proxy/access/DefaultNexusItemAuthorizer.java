package org.sonatype.nexus.proxy.access;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.SecurityUtils;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.proxy.router.RootRepositoryRouter;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * Default implementation of Nexus Authorizer, that relies onto JSecurity.
 */
@Component( role = NexusItemAuthorizer.class )
public class DefaultNexusItemAuthorizer
    implements NexusItemAuthorizer
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement( role = RootRepositoryRouter.class )
    private RepositoryRouter root;

    public boolean authorizePath( RepositoryItemUid uid, Map<String, Object> context, Action action )
    {
        // Get the target(s) that match the path
        TargetSet matched = uid.getRepository().getTargetsForRequest( uid, context );

        return authorizePath( matched, action );
    }

    public boolean authorizePath( ResourceStoreRequest rsr, Action action )
    {
        TargetSet matched = root.getTargetsForRequest( rsr );

        return authorizePath( matched, action );
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

    protected boolean isPermitted( String[] perms )
    {
        // Get the current user
        Subject subject = SecurityUtils.getSubject();

        if ( applicationConfiguration.isSecurityEnabled() )
        {
            // And finally check each of the target permissions and see if the user
            // has access, all it takes is one
            for ( String perm : perms )
            {
                if ( subject.isPermitted( perm ) )
                {
                    return true;
                }
            }
        }
        else
        {
            // sec is disabled, simply say YES
            return true;
        }

        return false;
    }

    protected String[] getTargetPerms( TargetSet matched, Action action )
    {
        String[] result = null;

        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId : read
        for ( TargetMatch match : matched.getMatches() )
        {
            perms
                .add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() + ":" + action );
        }

        result = perms.toArray( new String[perms.size()] );

        return result;
    }

}

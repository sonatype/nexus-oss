package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletRequest;

import org.jsecurity.SecurityUtils;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * @plexus.component
 */
public class DefaultNexusArtifactAuthorizer
    implements NexusArtifactAuthorizer
{
    
    /** @plexus.requirement */
    private Nexus nexus;
    
    public boolean authorizePath( Repository repository, String path )
    {
        // Get the target(s) that match the path
        TargetSet matched = repository.getTargetsForRequest( repository.createUid( path ), null );
        
        return isPermitted( getTargetPerms( matched, "read" ) );
    }
    
    public boolean authorizePath( ServletRequest request, ResourceStoreRequest rsr, String action )
    {
        // collect the targetSet/matches for the request
        TargetSet matched = getNexus().getRootRouter().getTargetsForRequest( rsr );
    
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

        
        if ( getNexus().isSecurityEnabled() )
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
            return true;

        }

        return false;
    }
    
    protected String[] getTargetPerms( TargetSet matched, String method )
    {
        String[] result = null;

        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId : read
        for ( TargetMatch match : matched.getMatches() )
        {
            perms.add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() + ":" + method );
        }

        result = perms.toArray( new String[perms.size()] );

        return result;
    }

    public Nexus getNexus()
    {
        return nexus;
    }
    
}

package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.jsecurity.SecurityUtils;
import org.jsecurity.subject.Subject;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.target.TargetMatch;
import org.sonatype.nexus.proxy.target.TargetSet;

/**
 * @plexus.component
 */
public class DefaultNexusArtifactAuthorizer
    extends AbstractLogEnabled
        implements NexusArtifactAuthorizer
{
    public boolean authorizePath( Repository repository, String path )
    {
        getLogger().debug( "Authorize Repository Path: " + repository.getId() + " : " + path );
        // Get the target(s) that match the path
        TargetSet matched = repository.getTargetsForRequest( repository.createUid( path ), null );
        
        // Build the permission strings for the target(s)
        String[] targetPerms = getTargetPerms( matched );
        
        // Get the current user
        Subject subject = SecurityUtils.getSubject();
        
        // And finally check each of the target permissions and see if the user
        // has access, all it takes is one
        for ( String perm : targetPerms )
        {
            if ( subject.isPermitted( perm ) )
            {
                getLogger().debug( "Permission : " + perm + " : is permitted");
                return true;
            }
            getLogger().debug( "Permission : " + perm + " : is NOT permitted");
        }
        
        return false;
    }
    
    protected String[] getTargetPerms( TargetSet matched )
    {
        String[] result = null;

        List<String> perms = new ArrayList<String>( matched.getMatches().size() );

        // nexus : 'target' + targetId : repoId : read
        for ( TargetMatch match : matched.getMatches() )
        {
            perms.add( "nexus:target:" + match.getTarget().getId() + ":" + match.getRepository().getId() + ":read" );
        }

        result = perms.toArray( new String[perms.size()] );

        return result;
    }
}

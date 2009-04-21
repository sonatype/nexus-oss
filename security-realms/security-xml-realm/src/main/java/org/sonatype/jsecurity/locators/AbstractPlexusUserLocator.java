package org.sonatype.jsecurity.locators;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.util.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;

public abstract class AbstractPlexusUserLocator
    implements PlexusUserLocator
{
    
    protected Set<PlexusUser> filterListInMemeory( Set<PlexusUser> users, PlexusUserSearchCriteria criteria )
    {
        HashSet<PlexusUser> result = new HashSet<PlexusUser>();

        for ( PlexusUser user : users )
        {
            if ( userMatchesCriteria( user, criteria ) )
            {
                // add the user if it matches the search criteria
                result.add( user );
            }
        }

        return result;
    }

    protected boolean userMatchesCriteria( PlexusUser user, PlexusUserSearchCriteria criteria )
    {
        if ( StringUtils.isNotEmpty( criteria.getUserId() )
            && !user.getUserId().toLowerCase().startsWith( criteria.getUserId().toLowerCase() ) )
        {
            return false;
        }
        
        if( criteria.getOneOfRoleIds() != null && !criteria.getOneOfRoleIds().isEmpty() )
        {
            Set<String> userRoles = new HashSet<String>();
            for ( PlexusRole role : user.getRoles() )
            {
                userRoles.add( role.getRoleId() );
            }
            
            // check the intersection of the roles
            if( CollectionUtils.intersection( criteria.getOneOfRoleIds(), userRoles ).isEmpty())
            {
                return false;
            }
        }
        
        return true;
    }
}

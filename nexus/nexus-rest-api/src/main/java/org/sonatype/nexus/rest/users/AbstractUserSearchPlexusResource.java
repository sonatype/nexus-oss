package org.sonatype.nexus.rest.users;

import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserManager;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.nexus.rest.model.PlexusUserListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusUserResource;

public abstract class AbstractUserSearchPlexusResource
    extends AbstractPlexusUserPlexusResource
{
    @Requirement( role = PlexusUserManager.class, hint = "additinalRoles" )
    private PlexusUserManager userManager;

    public static final String USER_SOURCE_KEY = "userSource";

    protected String getUserSource( Request request )
    {
        return request.getAttributes().get( USER_SOURCE_KEY ).toString();
    }

    protected PlexusUserListResourceResponse search( PlexusUserSearchCriteria criteria, String source )
    {
        PlexusUserListResourceResponse result = new PlexusUserListResourceResponse();

        Set<PlexusUser> users = userManager.searchUsers( criteria, source );
        
        for ( PlexusUser user : users )
        {
            PlexusUserResource res = nexusToRestModel( user );

            if ( res != null )
            {
                result.addData( res );
            }
        }

        return result;
    }

}

package org.sonatype.jsecurity.realms.url;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.locators.AbstractPlexusUserLocator;
import org.sonatype.jsecurity.locators.ConfiguredUsersPlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusRole;
import org.sonatype.jsecurity.locators.users.PlexusUser;
import org.sonatype.jsecurity.locators.users.PlexusUserLocator;
import org.sonatype.jsecurity.locators.users.PlexusUserSearchCriteria;
import org.sonatype.jsecurity.realms.tools.ConfigurationManager;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUserRoleMapping;

@Component( role = PlexusUserLocator.class, hint = "url", description = "URL Realm Users" )
public class URLUserLocator
    extends AbstractPlexusUserLocator
{
    public static final String SOURCE = "url";

    @Configuration( value = "${url-authentication-email-domain}" )
    private String emailDomain = "apache.org";

    @Configuration( value = "${url-authentication-default-role}" )
    private String defaultRole = "default-url-role";

    @Requirement( role = ConfigurationManager.class, hint = "resourceMerging" )
    private ConfigurationManager configuration;

    @Requirement(role = PlexusUserLocator.class )
    private List<PlexusUserLocator> userLocators;

    public String getSource()
    {
        return SOURCE;
    }

    public PlexusUser getUser( String userId )
    {
        // list of users to ignore
        Set<String> ignoredUsers = this.getIgnoredUserIds();

        if ( ignoredUsers.contains( userId ) )
        {
            return null;
        }

        // otherwise search for the user, the search will fake the user if its not in the security.xml
        Set<PlexusUser> users = this.searchUsers( new PlexusUserSearchCriteria( userId ) );

        // now find the user
        for ( PlexusUser plexusUser : users )
        {
            if ( plexusUser.getUserId().equals( userId ) )
            {
                return plexusUser;
            }
        }

        return null;
    }

    public boolean isPrimary()
    {
        return true;
    }

    public Set<PlexusUser> listUsers()
    {
        Set<PlexusUser> users = new HashSet<PlexusUser>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( SOURCE.equals( userRoleMapping.getSource() ) )
            {
                PlexusUser user = this.toPlexusUser( userRoleMapping.getUserId() );
                if ( user != null )
                {
                    users.add( user );
                }
            }
        }

        return users;
    }

    public Set<String> listUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        List<SecurityUserRoleMapping> userRoleMappings = this.configuration.listUserRoleMappings();
        for ( SecurityUserRoleMapping userRoleMapping : userRoleMappings )
        {
            if ( SOURCE.equals( userRoleMapping.getSource() ) )
            {
                String userId = userRoleMapping.getUserId();
                if ( StringUtils.isNotEmpty( userId ) )
                {
                    userIds.add( userId );
                }
            }
        }

        return userIds;
    }

    public Set<PlexusUser> searchUsers( PlexusUserSearchCriteria criteria )
    {
        Set<PlexusUser> result = new HashSet<PlexusUser>();

        if ( StringUtils.isNotEmpty( criteria.getUserId() ) )
        {
            String userId = criteria.getUserId();

            // first we need to check if we need to ignore one of these users
            if ( this.getIgnoredUserIds().contains( userId ) )
            {
                return result;
            }

            for ( PlexusUser plexusUser : this.listUsers() )
            {
                if ( plexusUser.getUserId().toLowerCase().startsWith( userId.toLowerCase() ) )
                {
                    result.add( plexusUser );
                }
            }

            // this is a bit fuzzy, because we want to return a user even if we didn't find one
            // first check if we had an exact match

            PlexusUser exactUser = null;
            for ( PlexusUser plexusUser : result )
            {
                if ( plexusUser.getUserId().toLowerCase().equals( userId.toLowerCase() ) )
                {
                    exactUser = plexusUser;
                }
            }
            // if not exact user is found, fake it
            if ( exactUser == null )
            {
                result.add( this.toPlexusUser( userId ) );
            }
        }
        else
        {
            result = this.listUsers();
        }

        // this will the on things other then the userId
        return this.filterListInMemeory( result, criteria );
    }

    private PlexusUser toPlexusUser( String userId )
    {
        PlexusUser user = new PlexusUser();
        user.setEmailAddress( userId + "@" + emailDomain );
        user.setName( userId );
        user.setSource( SOURCE );
        user.setUserId( userId );

        PlexusRole role = new PlexusRole();
        role.setRoleId( this.defaultRole );
        role.setName( this.defaultRole );
        role.setSource( SOURCE );
        user.addRole( role );

        return user;
    }

    private Set<String> getIgnoredUserIds()
    {
        Set<String> userIds = new HashSet<String>();

        for ( PlexusUserLocator userLocator : this.userLocators )
        {
            if ( !this.getSource().equals( userLocator.getSource() ) && !ConfiguredUsersPlexusUserLocator.SOURCE.equals( userLocator.getSource() ) )
            {
                userIds.addAll( userLocator.listUserIds() );
            }
        }

        return userIds;
    }
}

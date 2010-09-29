package org.sonatype.nexus.proxy.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.credential.AllowAllCredentialsMatcher;
import org.jsecurity.authc.credential.CredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

public class PlexusConfiguredRealm
    extends AuthorizingRealm
{

    private Map<String, String> userPrivilageMap;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        String username = (String) principals.iterator().next();

        // check the userPrivilageMap key set for the user

        if ( StringUtils.isNotEmpty( username ) && this.userPrivilageMap.containsKey( username ) )
        {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

            // "nexus:target:" + <targetId> + ":" + <repoId> + ":" + <action>
            // String priv = "nexus:target:" + "*" + ":" + "repo1" + ":" + "*";
            info.addObjectPermissions( this.buildPermissions( this.userPrivilageMap.get( username ) ) );
            return info;
        }

        return null;
    }

    private List<Permission> buildPermissions( String commaSeperatedList )
    {
        String[] privs = commaSeperatedList.split( "," );
        List<Permission> permissions = new ArrayList<Permission>();

        for ( int ii = 0; ii < privs.length; ii++ )
        {
            permissions.add( new WildcardPermission( privs[ii].trim() ) );
        }
        return permissions;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        if ( this.userPrivilageMap.containsKey( token.getPrincipal().toString() ) )
        {
            return new SimpleAuthenticationInfo( token.getPrincipal().toString(), token.getCredentials(), this.getName() );
        }

        return null;
    }

    @Override
    public CredentialsMatcher getCredentialsMatcher()
    {
        return new AllowAllCredentialsMatcher();
    }
}

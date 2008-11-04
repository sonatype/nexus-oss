package org.sonatype.nexus.rest.authentication;

import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.SecurityUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.Subject;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;
import org.sonatype.nexus.rest.model.ClientPermission;

public abstract class AbstractUIPermissionCalculatingPlexusResource
    extends AbstractNexusPlexusResource
{
    private static final int NONE = 0;

    private static final int READ = 1;

    private static final int UPDATE = 2;

    private static final int DELETE = 4;

    private static final int CREATE = 8;

    private static final int ALL = READ | UPDATE | DELETE | CREATE;

    @Requirement
    private NexusSecurity nexusSecurity;

    protected AuthenticationClientPermissions getClientPermissionsForCurrentUser( Request request )
        throws ResourceException
    {
        AuthenticationClientPermissions perms = new AuthenticationClientPermissions();

        Subject subject = SecurityUtils.getSubject();

        if ( getNexus().isSecurityEnabled() )
        {
            if ( getNexus().isAnonymousAccessEnabled() )
            {
                // we must decide is the user logged in the anon user and we must tell "false" if it is
                if ( getNexus().getAnonymousUsername().equals( subject.getPrincipal() ) )
                {
                    perms.setLoggedIn( false );
                }
                else
                {
                    perms.setLoggedIn( true );
                }
            }
            else
            {
                // anon access is disabled, simply ask JSecurity about this
                perms.setLoggedIn( subject != null && subject.isAuthenticated() );
            }

            if ( perms.isLoggedIn() )
            {
                // try to set the loggedInUsername
                Object principal = subject.getPrincipal();

                if ( principal != null )
                {
                    perms.setLoggedInUsername( principal.toString() );
                }
            }
        }
        else
        {
            perms.setLoggedIn( true );

            perms.setLoggedInUsername( "anonymous" );
        }

        for ( SecurityPrivilege priv : nexusSecurity.listPrivileges() )
        {
            if ( priv.getType().equals( "method" ) )
            {
                String permission = nexusSecurity.getPrivilegeProperty( priv, "permission" );

                ClientPermission cPermission = new ClientPermission();
                cPermission.setId( permission );
                cPermission.setValue( getFlagsForPermission( subject, permission, request ) );

                perms.addPermission( cPermission );
            }
        }

        return perms;
    }

    protected int getFlagsForPermission( Subject subject, String domain, Request request )
        throws ResourceException
    {
        if ( subject == null )
        {
            if ( getNexus().isSecurityEnabled() )
            {
                // WTF? How is it here then?
                return NONE;
            }
            else
            {
                // Security is OFF
                return ALL;
            }
        }

        Permission readPerm = new WildcardPermission( domain + ":read" );

        Permission createPerm = new WildcardPermission( domain + ":create" );

        Permission updatePerm = new WildcardPermission( domain + ":update" );

        Permission deletePerm = new WildcardPermission( domain + ":delete" );

        int perm = NONE;

        if ( subject.isPermitted( readPerm ) )
        {
            perm |= READ;
        }
        if ( subject.isPermitted( createPerm ) )
        {
            perm |= CREATE;
        }
        if ( subject.isPermitted( updatePerm ) )
        {
            perm |= UPDATE;
        }
        if ( subject.isPermitted( deletePerm ) )
        {
            perm |= DELETE;
        }

        return perm;
    }
}

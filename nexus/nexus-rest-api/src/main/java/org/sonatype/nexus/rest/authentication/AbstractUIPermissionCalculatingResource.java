package org.sonatype.nexus.rest.authentication;

import org.jsecurity.SecurityUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.Subject;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.security.model.CPrivilege;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;

public abstract class AbstractUIPermissionCalculatingResource
    extends AbstractNexusResourceHandler
{
    private static final int NONE = 0;

    private static final int READ = 1;

    private static final int UPDATE = 2;

    private static final int DELETE = 4;

    private static final int CREATE = 8;

    private static final int ALL = READ | UPDATE | DELETE | CREATE;

    public AbstractUIPermissionCalculatingResource( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    protected AuthenticationClientPermissions getClientPermissionsForCurrentUser()
    {
        AuthenticationClientPermissions perms = new AuthenticationClientPermissions();

        Subject subject = SecurityUtils.getSubject();

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

        perms.setViewSearch( getFlagsForPermission( subject, "nexus:index" ) );

        perms.setViewUpdatedArtifacts( getFlagsForPermission( subject, "nexus:feeds" ) );

        perms.setViewCachedArtifacts( getFlagsForPermission( subject, "nexus:feeds" ) );

        perms.setViewDeployedArtifacts( getFlagsForPermission( subject, "nexus:feeds" ) );

        perms.setViewSystemChanges( getFlagsForPermission( subject, "nexus:feeds" ) );

        perms.setMaintLogs( getFlagsForPermission( subject, "nexus:logs" ) );

        perms.setMaintConfig( getFlagsForPermission( subject, "nexus:configuration" ) );

        perms.setMaintRepos( getFlagsForPermission( subject, "nexus:repostatus" ) );

        perms.setConfigServer( getFlagsForPermission( subject, "nexus:settings" ) );

        perms.setConfigGroups( getFlagsForPermission( subject, "nexus:repogroups" ) );

        perms.setConfigRules( getFlagsForPermission( subject, "nexus:routes" ) );

        perms.setConfigRepos( getFlagsForPermission( subject, "nexus:repositories" ) );

        perms.setConfigSchedules( getFlagsForPermission( subject, "nexus:tasks" ) );

        perms.setConfigUsers( getFlagsForPermission( subject, "nexus:users" ) );

        perms.setConfigRoles( getFlagsForPermission( subject, "nexus:roles" ) );

        perms.setConfigPrivileges( getFlagsForPermission( subject, "nexus:privileges" ) );

        perms.setConfigRepoTargets( getFlagsForPermission( subject, "nexus:targets" ) );

        perms.setActionChangePassword( getFlagsForPermission( subject, "nexus:userschangepw" ) );

        perms.setActionForgotPassword( getFlagsForPermission( subject, "nexus:usersforgotpw" ) );

        perms.setActionForgotUserid( getFlagsForPermission( subject, "nexus:usersforgotid" ) );

        perms.setActionResetPassword( getFlagsForPermission( subject, "nexus:usersreset" ) );

        perms.setActionEmptyTrash( getFlagsForPermission( subject, "nexus:wastebasket" ) );

        perms.setActionDeleteCache( getFlagsForPermission( subject, "nexus:cache" ) );

        perms.setActionRebuildAttribs( getFlagsForPermission( subject, "nexus:attributes" ) );

        perms.setActionRunTask( getFlagsForPermission( subject, "nexus:tasksrun" ) );

        perms.setActionUploadArtifact( getFlagsForPermission( subject, "nexus:artifact" ) );

        perms.setActionReindex( getFlagsForPermission( subject, "nexus:index" ) );

        perms.setActionChecksumSearch( getFlagsForPermission( subject, "nexus:identify" ) );

        return perms;
    }

    protected int getFlagsForPermission( Subject subject, String domain )
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

        Permission readPerm = new WildcardPermission( domain + ":" + CPrivilege.METHOD_READ );

        Permission createPerm = new WildcardPermission( domain + ":" + CPrivilege.METHOD_CREATE );

        Permission updatePerm = new WildcardPermission( domain + ":" + CPrivilege.METHOD_UPDATE );

        Permission deletePerm = new WildcardPermission( domain + ":" + CPrivilege.METHOD_DELETE );

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

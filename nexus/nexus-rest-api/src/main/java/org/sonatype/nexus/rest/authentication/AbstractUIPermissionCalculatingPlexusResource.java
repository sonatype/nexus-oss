package org.sonatype.nexus.rest.authentication;

import org.jsecurity.SecurityUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.subject.Subject;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;

public abstract class AbstractUIPermissionCalculatingPlexusResource
    extends AbstractNexusPlexusResource
{
    private static final int NONE = 0;

    private static final int READ = 1;

    private static final int UPDATE = 2;

    private static final int DELETE = 4;

    private static final int CREATE = 8;

    private static final int ALL = READ | UPDATE | DELETE | CREATE;

    protected AuthenticationClientPermissions getClientPermissionsForCurrentUser( Request request ) throws ResourceException
    {
        AuthenticationClientPermissions perms = new AuthenticationClientPermissions();

        Subject subject = SecurityUtils.getSubject();

        if ( getNexusInstance( request ).isSecurityEnabled() )
        {
            if ( getNexusInstance( request ).isAnonymousAccessEnabled() )
            {
                // we must decide is the user logged in the anon user and we must tell "false" if it is
                if ( getNexusInstance( request ).getAnonymousUsername().equals( subject.getPrincipal() ) )
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

        perms.setViewSearch( getFlagsForPermission( subject, "nexus:index", request ) );

        perms.setViewUpdatedArtifacts( getFlagsForPermission( subject, "nexus:feeds", request ) );

        perms.setViewCachedArtifacts( getFlagsForPermission( subject, "nexus:feeds", request ) );

        perms.setViewDeployedArtifacts( getFlagsForPermission( subject, "nexus:feeds", request ) );

        perms.setViewSystemChanges( getFlagsForPermission( subject, "nexus:feeds", request ) );

        perms.setMaintLogs( getFlagsForPermission( subject, "nexus:logs", request ) );

        perms.setMaintConfig( getFlagsForPermission( subject, "nexus:configuration", request ) );

        perms.setMaintRepos( getFlagsForPermission( subject, "nexus:repostatus", request ) );

        perms.setConfigServer( getFlagsForPermission( subject, "nexus:settings", request ) );

        perms.setConfigGroups( getFlagsForPermission( subject, "nexus:repogroups", request ) );

        perms.setConfigRules( getFlagsForPermission( subject, "nexus:routes", request ) );

        perms.setConfigRepos( getFlagsForPermission( subject, "nexus:repositories", request ) );

        perms.setConfigSchedules( getFlagsForPermission( subject, "nexus:tasks", request ) );

        perms.setConfigUsers( getFlagsForPermission( subject, "nexus:users", request ) );

        perms.setConfigRoles( getFlagsForPermission( subject, "nexus:roles", request ) );

        perms.setConfigPrivileges( getFlagsForPermission( subject, "nexus:privileges", request ) );

        perms.setConfigRepoTargets( getFlagsForPermission( subject, "nexus:targets" , request ) );

        perms.setActionChangePassword( getFlagsForPermission( subject, "nexus:userschangepw", request ) );

        perms.setActionForgotPassword( getFlagsForPermission( subject, "nexus:usersforgotpw", request ) );

        perms.setActionForgotUserid( getFlagsForPermission( subject, "nexus:usersforgotid", request ) );

        perms.setActionResetPassword( getFlagsForPermission( subject, "nexus:usersreset", request ) );

        perms.setActionEmptyTrash( getFlagsForPermission( subject, "nexus:wastebasket", request ) );

        perms.setActionDeleteCache( getFlagsForPermission( subject, "nexus:cache", request ) );

        perms.setActionRebuildAttribs( getFlagsForPermission( subject, "nexus:attributes", request ) );

        perms.setActionRunTask( getFlagsForPermission( subject, "nexus:tasksrun", request ) );

        perms.setActionUploadArtifact( getFlagsForPermission( subject, "nexus:artifact", request ) );

        perms.setActionReindex( getFlagsForPermission( subject, "nexus:index", request ) );

        perms.setActionChecksumSearch( getFlagsForPermission( subject, "nexus:identify", request ) );

        return perms;
    }

    protected int getFlagsForPermission( Subject subject, String domain, Request request ) throws ResourceException
    {
        if ( subject == null )
        {
            if ( getNexusInstance( request ).isSecurityEnabled() )
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

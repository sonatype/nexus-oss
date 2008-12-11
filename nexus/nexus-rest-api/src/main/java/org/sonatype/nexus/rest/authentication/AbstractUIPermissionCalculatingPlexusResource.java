/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.SecurityUtils;
import org.jsecurity.authz.Permission;
import org.jsecurity.authz.permission.WildcardPermission;
import org.jsecurity.crypto.hash.Hash;
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

        Map<String, Integer> privilegeMap = new HashMap<String, Integer>();

        for ( SecurityPrivilege priv : nexusSecurity.listPrivileges() )
        {
            if ( priv.getType().equals( "method" ) )
            {
                String permission = nexusSecurity.getPrivilegeProperty( priv, "permission" );

                // check this shit her
                // FAIL COMPILE
                // subject.checkPermissions( permissions )
                privilegeMap.put( permission, NONE );

            }
        }

        // this will update the privilegeMap
        this.checkSubjectsPermissions( subject, privilegeMap );

        for ( Entry<String, Integer> privEntry : privilegeMap.entrySet() )
        {
            ClientPermission cPermission = new ClientPermission();
            cPermission.setId( privEntry.getKey() );
            cPermission.setValue( privEntry.getValue() );

            perms.addPermission( cPermission );
        }

        return perms;
    }

    private void checkSubjectsPermissions( Subject subject, Map<String, Integer> privilegeMap )
    {
        List<Permission> permissionList = new ArrayList<Permission>();
        List<String> permissionNameList = new ArrayList<String>();

        for ( Entry<String, Integer> priv : privilegeMap.entrySet() )
        {
            permissionList.add( new WildcardPermission( priv.getKey() + ":read" ) );
            permissionList.add( new WildcardPermission( priv.getKey() + ":create" ) );
            permissionList.add( new WildcardPermission( priv.getKey() + ":update" ) );
            permissionList.add( new WildcardPermission( priv.getKey() + ":delete" ) );
            permissionNameList.add( priv.getKey() + ":read" );
            permissionNameList.add( priv.getKey() + ":create" );
            permissionNameList.add( priv.getKey() + ":update" );
            permissionNameList.add( priv.getKey() + ":delete" );
        }

        if ( subject != null )
        {

            // get the privileges for this subject
            boolean[] boolResults = subject.isPermitted( permissionList );

            // put then in a map so we can access them easily
            Map<String, Boolean> resultMap = new HashMap<String, Boolean>();
            for ( int ii = 0; ii < permissionList.size(); ii++ )
            {
                String permissionName = permissionNameList.get( ii );
                boolean b = boolResults[ii];
                resultMap.put( permissionName, b );
            }

            // now loop through the original set and figure out the correct value
            for ( Entry<String, Integer> priv : privilegeMap.entrySet() )
            {

                boolean readPriv = resultMap.get( priv.getKey() + ":read" );
                boolean createPriv = resultMap.get( priv.getKey() + ":create" );
                boolean updaetPriv = resultMap.get( priv.getKey() + ":update" );
                boolean deletePriv = resultMap.get( priv.getKey() + ":delete" );

                int perm = NONE;

                if ( readPriv )
                {
                    perm |= READ;
                }
                if ( createPriv )
                {
                    perm |= CREATE;
                }
                if ( updaetPriv )
                {
                    perm |= UPDATE;
                }
                if ( deletePriv )
                {
                    perm |= DELETE;
                }
                // now set the value
                priv.setValue( perm );
            }
        }
        else
        {// subject is null
            // we should not have got here if security is not enabled.
            int value = getNexus().isSecurityEnabled() ? NONE : ALL;
            for ( Entry<String, Integer> priv : privilegeMap.entrySet() )
            {
                priv.setValue( value );
            }
        }

    }
}

/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.authorization;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;

/**
 * A implementation of the Shiro ModularRealmAuthorizer, that catches exceptions caused by individual realms and ignores
 * them. For example if a JDBC realm throws an exception while getting the list of users Roles (and is not caught, the
 * system should continue looking for permissions in other realms).
 */

public class ExceptionCatchingModularRealmAuthorizer
    extends ModularRealmAuthorizer
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public ExceptionCatchingModularRealmAuthorizer( Collection<Realm> realms )
    {
        super( realms );
    }

    @Inject
    public ExceptionCatchingModularRealmAuthorizer( Collection<Realm> realms,
                                                    @Nullable RolePermissionResolver rolePermissionResolver )
    {
        super( realms );

        if ( null != rolePermissionResolver )
        {
            setRolePermissionResolver( rolePermissionResolver );
        }
    }

    // Authorization
    @Override
    public void checkPermission( PrincipalCollection subjectPrincipal, String permission )
        throws AuthorizationException
    {
        if ( !this.isPermitted( subjectPrincipal, permission ) )
        {
            throw new AuthorizationException( "User is not permitted: " + permission );
        }
    }

    @Override
    public void checkPermission( PrincipalCollection subjectPrincipal, Permission permission )
        throws AuthorizationException
    {
        if ( !this.isPermitted( subjectPrincipal, permission ) )
        {
            throw new AuthorizationException( "User is not permitted: " + permission );
        }
    }

    @Override
    public void checkPermissions( PrincipalCollection subjectPrincipal, String... permissions )
        throws AuthorizationException
    {
        for ( String permission : permissions )
        {
            checkPermission( subjectPrincipal, permission );
        }
    }

    @Override
    public void checkPermissions( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
        throws AuthorizationException
    {
        for ( Permission permission : permissions )
        {
            checkPermission( subjectPrincipal, permission );
        }
    }

    @Override
    public void checkRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
        throws AuthorizationException
    {
        if ( !this.hasRole( subjectPrincipal, roleIdentifier ) )
        {
            throw new AuthorizationException( "User is not permitted role: " + roleIdentifier );
        }
    }

    @Override
    public void checkRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
        throws AuthorizationException
    {
        if ( !this.hasAllRoles( subjectPrincipal, roleIdentifiers ) )
        {
            throw new AuthorizationException( "User is not permitted role: " + roleIdentifiers );
        }
    }

    @Override
    public boolean hasAllRoles( PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers )
    {
        for ( String roleIdentifier : roleIdentifiers )
        {
            if ( !hasRole( subjectPrincipal, roleIdentifier ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean hasRole( PrincipalCollection subjectPrincipal, String roleIdentifier )
    {
        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            // need to catch an AuthorizationException, the user might only belong to on of the realms
            try
            {
                if ( ( (Authorizer) realm ).hasRole( subjectPrincipal, roleIdentifier ) )
                {
                    return true;
                }
            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return false;
    }

    @Override
    public boolean[] hasRoles( PrincipalCollection subjectPrincipal, List<String> roleIdentifiers )
    {
        boolean[] combinedResult = new boolean[roleIdentifiers.size()];

        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            try
            {
                boolean[] result = ( (Authorizer) realm ).hasRoles( subjectPrincipal, roleIdentifiers );

                for ( int i = 0; i < combinedResult.length; i++ )
                {
                    combinedResult[i] = combinedResult[i] | result[i];
                }

            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return combinedResult;
    }

    @Override
    public boolean isPermitted( PrincipalCollection subjectPrincipal, String permission )
    {
        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            try
            {
                if ( ( (Authorizer) realm ).isPermitted( subjectPrincipal, permission ) )
                {
                    if ( logger.isTraceEnabled() )
                    {
                        this.logger.trace( "Realm: " + realm.getName() + " user: " + subjectPrincipal.iterator().next()
                            + " has permisison: " + permission );
                    }
                    return true;
                }
                else
                {
                    if ( logger.isTraceEnabled() )
                    {
                        this.logger.trace( "Realm: " + realm.getName() + " user: " + subjectPrincipal.iterator().next()
                            + " does NOT have permisison: " + permission );
                    }
                }

            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return false;
    }

    @Override
    public boolean isPermitted( PrincipalCollection subjectPrincipal, Permission permission )
    {
        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            try
            {
                if ( ( (Authorizer) realm ).isPermitted( subjectPrincipal, permission ) )
                {
                    return true;
                }
            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return false;
    }

    @Override
    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, String... permissions )
    {
        boolean[] combinedResult = new boolean[permissions.length];

        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            try
            {
                boolean[] result = ( (Authorizer) realm ).isPermitted( subjectPrincipal, permissions );

                for ( int i = 0; i < combinedResult.length; i++ )
                {
                    combinedResult[i] = combinedResult[i] | result[i];
                }
            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return combinedResult;
    }

    @Override
    public boolean[] isPermitted( PrincipalCollection subjectPrincipal, List<Permission> permissions )
    {
        boolean[] combinedResult = new boolean[permissions.size()];

        for ( Realm realm : this.getRealms() )
        {
            if ( !( realm instanceof Authorizer ) )
            {
                continue; // ignore non-authorizing realms
            }
            try
            {
                boolean[] result = ( (Authorizer) realm ).isPermitted( subjectPrincipal, permissions );

                for ( int i = 0; i < combinedResult.length; i++ )
                {
                    combinedResult[i] = combinedResult[i] | result[i];
                }
            }
            catch ( AuthorizationException e )
            {
                logAndIgnore( realm, e );
            }
            catch ( RuntimeException e )
            {
                logAndIgnore( realm, e );
            }
        }

        return combinedResult;
    }

    @Override
    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, String... permissions )
    {
        for ( String permission : permissions )
        {
            if ( !isPermitted( subjectPrincipal, permission ) )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isPermittedAll( PrincipalCollection subjectPrincipal, Collection<Permission> permissions )
    {
        for ( Permission permission : permissions )
        {
            if ( !isPermitted( subjectPrincipal, permission ) )
            {
                return false;
            }
        }

        return true;
    }

    private void logAndIgnore( Realm realm, Exception e )
    {
        if ( logger.isTraceEnabled() )
        {
            logger.trace( "Realm: '" + realm.getName() + "', caused: " + e.getMessage(), e );
        }
    }
}

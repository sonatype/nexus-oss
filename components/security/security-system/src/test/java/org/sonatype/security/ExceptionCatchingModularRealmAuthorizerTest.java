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
package org.sonatype.security;

import java.util.Collections;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

public class ExceptionCatchingModularRealmAuthorizerTest
{
    private static final AuthorizingRealm BROKEN_REALM = new AuthorizingRealm()
    {
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
            throws AuthenticationException
        {
            throw new RuntimeException( "This realm only throws exceptions" );
        }

        @Override
        protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
        {
            throw new RuntimeException( "This realm only throws exceptions" );
        }
    };

    @Test
    public void ignoreRuntimeException()
        throws Exception
    {
        ExceptionCatchingModularRealmAuthorizer subject =
            new ExceptionCatchingModularRealmAuthorizer( Collections.<Realm> singleton( BROKEN_REALM ) );

        Permission permission = new AllPermission();

        Assert.assertFalse( subject.isPermitted( (PrincipalCollection) null, "" ) );
        Assert.assertFalse( subject.isPermitted( (PrincipalCollection) null, permission ) );
        Assert.assertFalse( subject.isPermitted( (PrincipalCollection) null, new String[] { "" } )[0] );
        Assert.assertFalse( subject.isPermitted( (PrincipalCollection) null, Collections.singletonList( permission ) )[0] );
    }
}

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

import javax.enterprise.inject.Typed;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;

/**
 * A permission factory that creates instances of Shiro's {@link WildcardPermission} by directly invoking it's
 * constructor with passed in string representation of the permission. This is the default factory, as the
 * {@link WildcardPermission} is the default permission implementation used all over Security.
 * 
 * @author cstamas
 * @since 2.8
 */
@Named( "wildcard" )
@Singleton
@Typed( PermissionFactory.class )
public class WildcardPermissionFactory
    implements PermissionFactory
{
    @Override
    public Permission create( final String permission )
    {
        return new WildcardPermission( permission );
    }
}

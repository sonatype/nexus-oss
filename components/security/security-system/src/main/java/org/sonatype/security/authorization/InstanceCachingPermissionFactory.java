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

import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authz.Permission;

import com.google.common.collect.MapMaker;

/**
 * Permission factory that delegates to another factory, and caches returned instances keyed by permission string
 * representation into weak map. The trick is, that permissions themself may be considered "static". The application
 * defines them upfront, and they are constant during runtime. The mapping of users to permissions (using different
 * concepts like "roles" or "groups") are volatile, but this factory has nothing to do with mapping, and even then, the
 * mapped permissions are still constants.
 * 
 * @author cstamas
 * @since 2.8
 */
@Named( "caching" )
@Singleton
@Typed( PermissionFactory.class )
public class InstanceCachingPermissionFactory
    implements PermissionFactory
{
    private final ConcurrentMap<String, Permission> instances;

    private final PermissionFactory delegate;

    @Inject
    public InstanceCachingPermissionFactory( @Named( "wildcard" ) final PermissionFactory delegate )
    {
        this.instances = new MapMaker().weakValues().makeMap();
        this.delegate = delegate;
    }

    @Override
    public Permission create( final String permission )
    {
        return getOrCreate( permission );
    }

    // ==

    protected Permission getOrCreate( final String permission )
    {
        Permission result = instances.get( permission );
        if ( result == null )
        {
            Permission newPermission = delegateCreate( permission );
            result = instances.putIfAbsent( permission, newPermission );
            if ( result == null )
            {
                // put succeeded, use new value
                result = newPermission;
            }
        }
        return result;
    }

    protected Permission delegateCreate( final String permission )
    {
        return delegate.create( permission );
    }
}

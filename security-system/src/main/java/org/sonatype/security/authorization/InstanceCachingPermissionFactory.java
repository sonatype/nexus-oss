package org.sonatype.security.authorization;

import java.util.concurrent.ConcurrentMap;

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
public class InstanceCachingPermissionFactory
    implements PermissionFactory
{
    private final ConcurrentMap<String, Permission> instances;

    private final PermissionFactory delegate;

    @Inject
    public InstanceCachingPermissionFactory( final PermissionFactory delegate )
    {
        this.instances = new MapMaker().weakValues().makeMap();
        this.delegate = delegate;
    }

    @Override
    public Permission create( final String permission )
    {
        return getOrCreate( permission.intern() );
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

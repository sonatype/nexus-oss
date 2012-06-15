package org.sonatype.security.authorization;

import org.apache.shiro.authz.Permission;

/**
 * A permission factory that creates Permission instances. It may apply other stuff, like caching instances for example,
 * based on permission string representation. This is just a concept to be able to hide caching of it. Which
 * implementation you use, depends on your app very much, but usually you'd want the one producing the most widely used
 * permission in Shiro: the {@link WildcardPermissionFactory}.
 * 
 * @author cstamas
 * @since 2.8
 */
public interface PermissionFactory
{
    Permission create( final String permission );
}

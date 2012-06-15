package org.sonatype.security.authorization;

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
@Named
@Singleton
public class WildcardPermissionFactory
    implements PermissionFactory
{
    @Override
    public Permission create( String permission )
    {
        return new WildcardPermission( permission );
    }
}

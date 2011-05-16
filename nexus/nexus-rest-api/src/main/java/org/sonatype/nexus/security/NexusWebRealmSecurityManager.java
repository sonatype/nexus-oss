package org.sonatype.nexus.security;

import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.slf4j.Logger;
import org.sonatype.security.web.WebRealmSecurityManager;

/**
 * An extension of WebRealmSecurityManager used because we have a mix of POJO's and @Inject ojbects
 */
@Singleton
@Typed( value = RealmSecurityManager.class )
@Named( value = "nexus" )
public class NexusWebRealmSecurityManager
    extends WebRealmSecurityManager
    implements org.apache.shiro.util.Initializable
{
    @Inject
    public NexusWebRealmSecurityManager( Logger logger, Map<String, RolePermissionResolver> rolePermissionResolverMap )
    {
        super( logger, rolePermissionResolverMap );
    }    

    public void init()
        throws ShiroException
    {
        // use cacheing for the sessions, we can tune this with a props file per application if needed
        StatelessAndStatefulWebSessionManager webSessionManager = new StatelessAndStatefulWebSessionManager();
        webSessionManager.setSessionDAO( new EnterpriseCacheSessionDAO() );
        this.setSessionManager( webSessionManager );
    }
}

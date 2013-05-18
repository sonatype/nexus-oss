/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
import org.slf4j.LoggerFactory;
import org.sonatype.security.web.WebRealmSecurityManager;

/**
 * An extension of WebRealmSecurityManager used because we have a mix of POJO's and @Inject objects.
 * @deprecated replaced by applying the noSessionCreation filter from Shiro 1.2 to non-login services.
 */
@Singleton
@Typed( value = RealmSecurityManager.class )
@Named( value = "stateless-and-stateful" )
@Deprecated
public class NexusWebRealmSecurityManager
    extends WebRealmSecurityManager
    implements org.apache.shiro.util.Initializable
{
    @Inject
    public NexusWebRealmSecurityManager( Map<String, RolePermissionResolver> rolePermissionResolverMap )
    {
        super( rolePermissionResolverMap );
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

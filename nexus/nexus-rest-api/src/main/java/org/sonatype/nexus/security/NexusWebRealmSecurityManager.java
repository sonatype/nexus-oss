/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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

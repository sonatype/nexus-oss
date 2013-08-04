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

package org.sonatype.nexus.web;

import javax.servlet.ServletContext;

import org.sonatype.security.web.guice.SecurityWebModule;

import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.shiro.mgt.RealmSecurityManager;

/**
 * Custom {@link SecurityWebModule} for Nexus.
 */
public class NexusWebModule
    extends SecurityWebModule
{
  public NexusWebModule(ServletContext servletContext) {
    super(servletContext, true);
  }

  @Override
  protected void configureShiroWeb() {
    super.configureShiroWeb();

        /*
         * -----------------------------------------------------------------------------------------------------------
         * Expose an explicit binding to replace the old stateless and stateful "nexus" RealmSecurityManager with the
         * default RealmSecurityManager, since we now use the "noSessionCreation" filter in Shiro 1.2 on all services
         * except the login service.
         * -----------------------------------------------------------------------------------------------------------
         * The NexusWebRealmSecurityManager is still available (if necessary) under the "stateless-and-stateful" hint.
         * -----------------------------------------------------------------------------------------------------------
         */

    Named nexus = Names.named("nexus");
    bind(RealmSecurityManager.class).annotatedWith(nexus).to(RealmSecurityManager.class);
    expose(RealmSecurityManager.class).annotatedWith(nexus);
  }
}

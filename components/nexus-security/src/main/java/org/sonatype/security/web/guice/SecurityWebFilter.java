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

package org.sonatype.security.web.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.sonatype.security.SecuritySystem;

import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.ShiroFilter;

/**
 * Injected {@link ShiroFilter} that only applies when {@link SecuritySystem#isSecurityEnabled()} is {@code true}.
 */
@Singleton
public class SecurityWebFilter
    extends AbstractShiroFilter
{
  private final SecuritySystem securitySystem;

  @Inject
  protected SecurityWebFilter(SecuritySystem securitySystem, FilterChainResolver filterChainResolver) {
    this.securitySystem = securitySystem;
    this.setSecurityManager((WebSecurityManager) securitySystem.getSecurityManager());
    this.setFilterChainResolver(filterChainResolver);
  }

  @Override
  public boolean isEnabled() {
    return securitySystem.isSecurityEnabled();
  }
}

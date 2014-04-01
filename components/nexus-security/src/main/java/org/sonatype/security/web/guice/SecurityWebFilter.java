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

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.SecuritySystem;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.ShiroFilter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Injected {@link ShiroFilter}.
 */
@Singleton
public class SecurityWebFilter
    extends AbstractShiroFilter
{
  private static final Logger LOG = LoggerFactory.getLogger(SecurityWebFilter.class);
  private static final String USERNAME_HEADER = "X-USERNAME";

  @Inject
  protected SecurityWebFilter(SecuritySystem securitySystem, FilterChainResolver filterChainResolver) {
    this.setSecurityManager((WebSecurityManager) checkNotNull(securitySystem.getSecurityManager(), "securityManager"));
    this.setFilterChainResolver(checkNotNull(filterChainResolver, "filterChainResolver"));
  }

  /**
   * Executes a {@link FilterChain} for the given request.
   * <p/>
   * This implementation first delegates to
   * <code>{@link #getExecutionChain(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain) getExecutionChain}</code>
   * to allow the application's Shiro configuration to determine exactly how the chain should execute.  The resulting
   * value from that call is then executed directly by calling the returned {@code FilterChain}'s
   * {@link FilterChain#doFilter doFilter} method.  That is:
   * <pre>
   * FilterChain chain = {@link #getExecutionChain}(request, response, origChain);
   * chain.{@link FilterChain#doFilter doFilter}(request,response);</pre>
   *
   * @param request   the incoming ServletRequest
   * @param response  the outgoing ServletResponse
   * @param origChain the Servlet Container-provided chain that may be wrapped further by an application-configured
   *                  chain of Filters.
   * @throws IOException      if the underlying {@code chain.doFilter} call results in an IOException
   * @throws ServletException if the underlying {@code chain.doFilter} call results in a ServletException
   * @since 1.0
   */
  @Override
  protected void executeChain(ServletRequest request, ServletResponse response, FilterChain origChain) throws IOException, ServletException {
    addUserInHeader(response);
	
    super.executeChain(request, response, origChain);
  }

  private void addUserInHeader(ServletResponse response) {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String username = null;
    Subject currentUser = SecurityUtils.getSubject();

    LOG.debug(String.valueOf(currentUser));

    Object principal = currentUser.getPrincipal();
    if (principal != null) {
      username = principal.toString().trim();
    }

    if (username == null || username.trim().isEmpty()) {
      username = "-";
    } else {
      httpResponse.addHeader(USERNAME_HEADER, username);
    }
    LOG.debug(username);
  }

}

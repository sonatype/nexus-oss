/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

// NOTE: This would be better integrated as part of the org.sonatype.security.web.guice.SecurityWebFilter ?

/**
 * Servlet filter to add user context details to the {@link MDC}.
 *
 * @since 2.5
 */
@Named
@Singleton
public class MdcUserContextFilter
    implements Filter
{
    private static final Logger log = LoggerFactory.getLogger(MdcUserContextFilter.class);

    public static final String USER_ID = "userId";

    public static final String UNKNOWN_USER_ID = "<unknown-user>";

    @Override
    public void init(final FilterConfig config) throws ServletException {
        // ignore
    }

    @Override
    public void destroy() {
        // ignore
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        MDC.put(USER_ID, getCurrentUserId());

        try {
            chain.doFilter(request, response);
        }
        finally {
            MDC.remove(USER_ID);
        }
    }

    private String getCurrentUserId() {
        String userId = UNKNOWN_USER_ID;

        try {
            Subject subject = SecurityUtils.getSubject();
            if (subject != null) {
                Object principal = subject.getPrincipal();
                if (principal != null) {
                    userId = principal.toString();
                }
            }
        }
        catch (Exception e) {
            log.warn("Unable to determine current user; ignoring", e);
        }

        log.trace("Current userId: {}", userId);

        return userId;
    }
}

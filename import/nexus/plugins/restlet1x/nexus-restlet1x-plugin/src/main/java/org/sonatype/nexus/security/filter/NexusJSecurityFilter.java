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
package org.sonatype.nexus.security.filter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.security.web.ShiroSecurityFilter;

/**
 * This filter simply behaves according Nexus configuration.
 * 
 * @author cstamas
 * @deprecated replaced with {@link org.sonatype.security.web.guice.SecurityWebFilter}.
 */
@Deprecated
public class NexusJSecurityFilter
    extends ShiroSecurityFilter
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String REQUEST_IS_AUTHZ_REJECTED = "request.is.authz.rejected";

    public NexusJSecurityFilter()
    {
        logger.info( "@Deprecated replaced with org.sonatype.security.web.guice.SecurityWebFilter" );

        // not setting configClassName explicitly, so we can use either configRole or configClassName
    }

    @Override
    protected boolean shouldNotFilter( ServletRequest request )
        throws ServletException
    {
        return !( (NexusConfiguration) getAttribute( NexusConfiguration.class.getName() ) ).isSecurityEnabled();
    }

    protected Object getAttribute( String key )
    {
        return this.getFilterConfig().getServletContext().getAttribute( key );
    }
    
    @Override
    protected String getWebSecurityManagerName()
    {
        return "nexus";
    }
}

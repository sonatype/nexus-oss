/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.web;

import java.util.Enumeration;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.sonatype.plexus.rest.PlexusServerServlet;

/**
 * An {@link PlexusServerServlet} that has an hardcoded name of "nexus" as required by plexus init param lookup. Guice
 * servlet extension does not allow servlet name setup while binding.
 * 
 * @author adreghiciu
 */
@Singleton
class NexusRestletServlet
    extends PlexusServerServlet
{

    private static final long serialVersionUID = -840934203229475592L;

    /**
     * Original servlet context delegate.
     */
    private DelegatingServletConfig servletConfig;

    NexusRestletServlet()
    {
        servletConfig = new DelegatingServletConfig();
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    /**
     * An {@link ServletConfig} delegate that has an hardcoded servlet name.
     */
    private class DelegatingServletConfig
        implements ServletConfig
    {

        public String getServletName()
        {
            return "nexus";
        }

        public ServletContext getServletContext()
        {
            return NexusRestletServlet.super.getServletConfig().getServletContext();
        }

        public String getInitParameter( String name )
        {
            return NexusRestletServlet.super.getServletConfig().getInitParameter( name );
        }

        @SuppressWarnings( "rawtypes" )
        public Enumeration getInitParameterNames()
        {
            return NexusRestletServlet.super.getServletConfig().getInitParameterNames();
        }
    }

}

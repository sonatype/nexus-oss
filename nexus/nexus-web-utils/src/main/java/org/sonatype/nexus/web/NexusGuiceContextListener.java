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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Initializes guice servlet extension {@see PlexusContainerContextListener#getInjector()}.
 * 
 * @author adreghiciu
 */
public class NexusGuiceContextListener
    extends GuiceServletContextListener
{

    private ServletContext servletContext;

    public void contextInitialized( ServletContextEvent sce )
    {
        servletContext = sce.getServletContext();
        super.contextInitialized( sce );
    }

    @Override
    protected Injector getInjector()
    {
        try
        {
            PlexusContainer plexusContainer =
                (PlexusContainer) servletContext.getAttribute( PlexusConstants.PLEXUS_KEY );
            return plexusContainer.lookup( Injector.class );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( "Could not locate Guice Injector.", e );
        }
    }

}

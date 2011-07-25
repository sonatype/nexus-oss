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
package org.sonatype.nexus.web;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextFactory;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.MapSourcedContextFiller;
import org.sonatype.appcontext.PropertiesFileContextFiller;
import org.sonatype.appcontext.SimpleBasedirDiscoverer;

/**
 * This ServeletContextListener boots up Plexus in a webapp environment, if needed. It is safe to have it multiple times
 * executed, since it will create only once, or reuse the found container.
 * 
 * @author cstamas
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    private AppContextFactory appContextFactory = new AppContextFactory();

    private PlexusContainer plexusContainer;

    private File plexusPropertiesFile;

    private File plexusXmlFile;

    public void contextInitialized( ServletContextEvent sce )
    {
        ServletContext context = sce.getServletContext();

        // create a container if there is none yet
        if ( context.getAttribute( PlexusConstants.PLEXUS_KEY ) == null )
        {
            try
            {
                AppContext plexusContext = createContainerContext( context );

                ContainerConfiguration plexusConfiguration =
                    new DefaultContainerConfiguration().setName( context.getServletContextName() ).setContainerConfigurationURL(
                        plexusXmlFile.toURI().toURL() ).setContext( plexusContext ).setAutoWiring( true ).setClassPathScanning(
                        PlexusConstants.SCANNING_ON ).setComponentVisibility( PlexusConstants.GLOBAL_VISIBILITY );

                plexusContainer = new DefaultPlexusContainer( plexusConfiguration );

                context.setAttribute( PlexusConstants.PLEXUS_KEY, plexusContainer );

                context.setAttribute( AppContext.class.getName(), plexusContext );
            }
            catch ( PlexusContainerException e )
            {
                sce.getServletContext().log( "Could not start Plexus container!", e );

                throw new IllegalStateException( "Could not start Plexus container!", e );
            }
            catch ( MalformedURLException e )
            {
                sce.getServletContext().log( "Could not start Plexus container!", e );

                throw new IllegalStateException( "Could not start Plexus container!", e );
            }
        }
    }

    public void contextDestroyed( ServletContextEvent sce )
    {
        if ( plexusContainer != null )
        {
            plexusContainer.dispose();
        }
    }

    // ==

    protected AppContext createContainerContext( ServletContext context )
        throws IllegalStateException
    {
        File basedirFile = null;
        File warWebInfFile = null;

        String baseDirProperty = System.getProperty( "bundleBasedir" );

        if ( !StringUtils.isEmpty( baseDirProperty ) )
        {
            // Nexus as bundle case
            context.log( "Setting Plexus basedir context variable to (pre-set in System properties): "
                + baseDirProperty );

            basedirFile = new File( baseDirProperty ).getAbsoluteFile();
        }

        String warWebInfFilePath = context.getRealPath( "/WEB-INF" );

        if ( !StringUtils.isEmpty( warWebInfFilePath ) )
        {
            warWebInfFile = new File( warWebInfFilePath ).getAbsoluteFile();

            if ( basedirFile == null )
            {
                context.log( "Setting Plexus basedir context variable to (discovered from Servlet container): "
                    + warWebInfFile );

                basedirFile = warWebInfFile;
            }
        }
        else
        {
            context.log( "CANNOT set Plexus basedir, Nexus cannot run from non-upacked WAR!" );

            throw new IllegalStateException( "Could not set Plexus basedir, Nexus cannot run from non-upacked WAR!" );
        }

        // plexus files are always here
        plexusPropertiesFile = new File( warWebInfFile, "plexus.properties" );
        plexusXmlFile = new File( warWebInfFile, "plexus.xml" );

        AppContextRequest request = appContextFactory.getDefaultAppContextRequest();

        // just pass over the already found basedir
        request.setBasedirDiscoverer( new SimpleBasedirDiscoverer( basedirFile ) );

        // add it to fillers as very 1st resource, and leaving others in
        request.getContextFillers().add( 0, new PropertiesFileContextFiller( plexusPropertiesFile, true ) );

        // add bundleBasedir (since "basedir") is automatically supported, but this one is not
        Map<Object, Object> ctx = new HashMap<Object, Object>();
        ctx.put( "bundleBasedir", basedirFile.getAbsolutePath() );
        request.getContextFillers().add( new MapSourcedContextFiller( ctx ) );

        try
        {
            AppContext response = appContextFactory.getAppContext( request );

            // wrap it in, to make Plexus friendly
            return response;
        }
        catch ( AppContextException e )
        {
            throw new IllegalStateException( e );
        }
    }
}

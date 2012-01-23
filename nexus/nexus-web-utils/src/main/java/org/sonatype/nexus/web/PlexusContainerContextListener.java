/**
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

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
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
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.source.MapEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.appcontext.source.StaticEntrySource;

import com.google.inject.Module;

/**
 * This ServeletContextListener boots up Plexus in a webapp environment, if needed. It is safe to have it multiple times
 * executed, since it will create only once, or reuse the found container.
 * 
 * @author cstamas
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
    public static final String CUSTOM_MODULES = "customModules";

    private PlexusContainer plexusContainer;

    /**
     * The one in bundle/conf/nexus.properties when ran as bundle, in WAR it does not exists.
     */
    private File nexusPropertiesFile;

    /**
     * The one in nexus/WEB-INF/nexus.properties, always exists
     */
    private File nexusDefaultPropertiesFile;

    /**
     * The plexus.xml file in nexus/WEB-INF/plexus.xml, always exists
     */
    private File plexusXmlFile;

    public void contextInitialized( final ServletContextEvent sce )
    {
        final ServletContext context = sce.getServletContext();

        // create a container if there is none yet
        if ( context.getAttribute( PlexusConstants.PLEXUS_KEY ) == null )
        {
            try
            {
                AppContext plexusContext =
                    createContainerContext( context,
                        (Map<String, Object>) context.getAttribute( AppContext.class.getName() ) );

                ContainerConfiguration plexusConfiguration =
                    new DefaultContainerConfiguration().setName( context.getServletContextName() ).setContainerConfigurationURL(
                        plexusXmlFile.toURI().toURL() ).setContext( (Map) plexusContext ).setAutoWiring( true ).setClassPathScanning(
                        PlexusConstants.SCANNING_INDEX ).setComponentVisibility( PlexusConstants.GLOBAL_VISIBILITY );

                final Module[] customModules = (Module[]) context.getAttribute( CUSTOM_MODULES );

                if ( customModules != null )
                {
                    plexusContainer = new DefaultPlexusContainer( plexusConfiguration, customModules );
                }
                else
                {
                    plexusContainer = new DefaultPlexusContainer( plexusConfiguration );
                }

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

    public void contextDestroyed( final ServletContextEvent sce )
    {
        if ( plexusContainer != null )
        {
            plexusContainer.dispose();
        }
    }

    // ==

    protected AppContext createContainerContext( final ServletContext context, final Map<String, Object> parent )
        throws AppContextException
    {
        if ( parent == null )
        {
            context.log( "Configuring Nexus in vanilla WAR..." );
        }
        else
        {
            context.log( "Configuring Nexus in bundle..." );
        }

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
        nexusDefaultPropertiesFile = new File( warWebInfFile, "plexus.properties" );
        plexusXmlFile = new File( warWebInfFile, "plexus.xml" );

        // no "real" parenting for now
        // for historical reasons, honor the "plexus" prefix too
        AppContextRequest request = Factory.getDefaultRequest( "nexus", null, Arrays.asList( "plexus" ) );

        // if in bundle only
        if ( parent != null && basedirFile != null )
        {
            nexusPropertiesFile = new File( basedirFile, "conf/nexus.properties" );

            // add the user overridable properties file, but it might not be present
            request.getSources().add( 0, new PropertiesFileEntrySource( nexusPropertiesFile, false ) );
        }

        // add the "defaults" properties files, must be present
        request.getSources().add( 0, new PropertiesFileEntrySource( nexusDefaultPropertiesFile, true ) );

        // add parent if found
        if ( parent != null )
        {
            // for now, once we resolve classloading issues....
            request.getSources().add( 0, new MapEntrySource( "quasiParent", parent ) );
        }

        // set basedir as LAST, no overrides for it
        request.getSources().add( new StaticEntrySource( "bundleBasedir", basedirFile.getAbsolutePath() ) );

        return Factory.create( request );
    }
}

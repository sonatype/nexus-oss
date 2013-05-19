/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.appcontext.servlet;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.lifecycle.Startable;
import org.sonatype.appcontext.lifecycle.Stoppable;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.PropertiesUrlEntrySource;

/**
 * AppContext servlet context listener. It creates a webapp-wide instance of {@link AppContext}, honoring presence of
 * parent, if any, and adding a default location to lookup properties used by {@link EntrySource}. The ID of the
 * {@link AppContext} is calculated in {@link #resolveContextId(ServletContext)}.
 * 
 * @author cstamas
 */
public class AppContextServletContextListener
    implements ServletContextListener
{
    /**
     * Parameter name to be used in {@code web.xml}'s {@code <context-param>} section, to override the default
     * calculation of {@link AppContext} ID. The default value is made from sanitized context path, by removing all
     * slash ("/") characters from context path and lower casing it. Example: context path "/NEXUS" will end up having
     * {@link AppContext} ID of "nexus", unless overridden.
     */
    public static final String APPCONTEXT_ID_PARAM = "appcontext-id";

    /**
     * Parameter name to be used in {@code web.xml}'s {@code <context-param>} section, to override the default location
     * of the AppContext properties file. The default value is "/WEB-INF/[appcontext.getId()].properties".
     */
    public static final String APPCONTEXT_PROPERTIES_PARAM = "appcontext-properties";

    /**
     * The default path to be used to look up the properties file.
     */
    private static final String DEFAULT_APPCONTEXT_PROPERTIES = "/WEB-INF/%s.properties";

    private AppContext webAppContext;

    public void contextInitialized( ServletContextEvent ctxe )
    {
        // get servlet context
        final ServletContext ctx = ctxe.getServletContext();
        // get the parent from attributes (this might be null!)
        final AppContext parent = (AppContext) ctx.getAttribute( AppContext.class.getName() );
        // figure out context ID
        final String contextId = resolveContextId( ctx );
        // create a default request with display-name as ID, possible parent (might be null)
        final AppContextRequest req = Factory.getDefaultRequest( contextId, parent );
        // add default context source as least important, as it leave room for override
        resolveContextEntrySource( req, ctx );
        // create an appcontext instance
        webAppContext = Factory.create( req );
        // put instance into servlet attributes (probably replacing parent if any)
        ctx.setAttribute( AppContext.class.getName(), webAppContext );
        // fire started
        webAppContext.getLifecycleManager().invokeHandler( Startable.class );
    }

    public void contextDestroyed( ServletContextEvent ctxe )
    {
        if ( webAppContext != null )
        {
            // fire stopped
            webAppContext.getLifecycleManager().invokeHandler( Stoppable.class );
        }
    }

    /**
     * Calculates the wanted {@link AppContext} ID. The default value is made from sanitized context path, by removing
     * all slash ("/") characters from context path and lower casing it. Example: context path "/NEXUS" will end up
     * having {@link AppContext} ID of "nexus", unless overridden. Exception is a root context (having path "/" or ""),
     * that will resolve to ID of "root", unless overridden.
     * 
     * @param context the context to use in resolution.
     * @return the ID string to be used as {@link AppContext} ID.
     */
    private String resolveContextId( final ServletContext context )
    {
        // check user given parameter
        String contextId = context.getInitParameter( APPCONTEXT_ID_PARAM );
        if ( contextId == null || contextId.trim().length() == 0 )
        {
            // default it
            contextId = context.getContextPath().replace( "/", "" ).toLowerCase();
        }
        if ( contextId == null || contextId.trim().length() == 0 )
        {
            // in case of root context (path "/" or "") we need a correction
            contextId = "root";
        }
        return contextId;
    }

    /**
     * Creates an {@link EntrySource} that is sourced from user specified location (resolved using
     * {@link ServletContext#getResource(String)}. If no user given parameter exists, it defaults to default path, that
     * is "/WEB-INF/contextId.properties".
     * 
     * @param request the app context creation request.
     * @param context the context to use in resolution.
     */
    private void resolveContextEntrySource( final AppContextRequest request, final ServletContext context )
    {
        String filename = context.getInitParameter( APPCONTEXT_PROPERTIES_PARAM );
        if ( filename == null || filename.trim().length() == 0 )
        {
            filename = String.format( DEFAULT_APPCONTEXT_PROPERTIES, request.getId() );
        }
        try
        {
            context.log( "Lookin up AppContext context properties from: \"" + filename + "\"" );
            final URL url = context.getResource( filename );
            request.getSources().add( 0, new PropertiesUrlEntrySource( url, false ) );
        }
        catch ( MalformedURLException e )
        {
            throw new IllegalArgumentException( "AppContext parameter \"" + APPCONTEXT_PROPERTIES_PARAM
                + "\" has invalid path set: \"" + filename + "\"", e );
        }
    }
}

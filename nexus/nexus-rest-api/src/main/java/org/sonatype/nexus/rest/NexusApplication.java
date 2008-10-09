/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.sonatype.jsecurity.web.PlexusMutableWebConfiguration;
import org.sonatype.jsecurity.web.PlexusWebConfiguration;
import org.sonatype.jsecurity.web.SecurityConfigurationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.PlexusResourceFinder;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

/**
 * Nexus REST Application. This will ultimately replace the two applications we have now, and provide us plugin UI
 * extension capability.
 * 
 * @author cstamas
 */
@Component( role = Application.class, hint = "nexus" )
public class NexusApplication
    extends PlexusRestletApplicationBridge
    implements EventListener
{
    @Requirement
    private Nexus nexus;

    @Requirement
    private PlexusWebConfiguration plexusWebConfiguration;

    @Requirement( hint = "nexusInstance" )
    private Filter nexusInstanceFilter;

    @Requirement( hint = "localNexusInstance" )
    private Filter localNexusInstanceFilter;

    @Requirement( hint = "indexTemplate" )
    private ManagedPlexusResource indexTemplateResource;

    @Requirement( hint = "content" )
    private ManagedPlexusResource contentResource;

    @Requirement( hint = "pluginResources" )
    private ManagedPlexusResource pluginResources;

    @Requirement( hint = "StatusPlexusResource" )
    private ManagedPlexusResource statusPlexusResource;

    @Requirement( hint = "CommandPlexusResource" )
    private ManagedPlexusResource commandPlexusResource;

    /**
     * Listener.
     */
    public void onProximityEvent( AbstractEvent evt )
    {
        if ( NexusStartedEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( true );
        }
        else if ( NexusStoppedEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( false );
        }
    }

    /**
     * Adding this as config change listener.
     */
    @Override
    protected void doConfigure()
    {
        nexus.getNexusConfiguration().addProximityEventListener( this );
    }

    /**
     * Configuring xstream with our aliases.
     */
    @Override
    protected XStream doConfigureXstream( XStream xstream )
    {
        return XStreamInitializer.initialize( xstream );
    }

    @Override
    protected Router initializeRouter( Router root, boolean isStarted )
    {
        // ===============
        // INITING FILTERS

        // instance filter, that injects proper Nexus instance into request attributes
        localNexusInstanceFilter.setContext( getContext() );

        // instance filter, that injects proper Nexus instance into request attributes
        nexusInstanceFilter.setContext( getContext() );

        // ==========
        // INDEX.HTML
        attach( root, false, indexTemplateResource.getResourceUri(), new PlexusResourceFinder(
            getContext(),
            indexTemplateResource ) );

        // ==========
        // PLUGIN "ADDED" RESOURCES
        attach( root, false, pluginResources.getResourceUri(), new PlexusResourceFinder( getContext(), pluginResources ) );

        // =======
        // CONTENT

        // prepare for browser diversity :)
        BrowserSensingFilter bsf = new BrowserSensingFilter( getContext() );

        // set the next as lnif
        bsf.setNext( localNexusInstanceFilter );

        // mounting it
        attach( root, false, "/content", bsf );

        // ========
        // SERVICE

        // service router
        Router applicationRouter = new Router( getContext() );

        // attaching service router after nif
        nexusInstanceFilter.setNext( applicationRouter );

        // attaching filter to a root on given URI
        attach( root, false, "/service/{" + NexusInstanceFilter.NEXUS_INSTANCE_KEY + "}", nexusInstanceFilter );

        // return the swapped router
        return applicationRouter;
    }

    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    @Override
    protected void doCreateRoot( Router applicationRouter, boolean isStarted )
    {
        // SERVICE (two always connected, unrelated to isStarted)

        attach( applicationRouter, false, statusPlexusResource );

        attach( applicationRouter, false, commandPlexusResource );

        if ( !isStarted )
        {
            // CONTENT, detaching all
            localNexusInstanceFilter.setNext( (Restlet) null );
        }
        else
        {
            // CONTENT, attaching it after nif
            localNexusInstanceFilter.setNext( new PlexusResourceFinder( getContext(), contentResource ) );
        }
    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource( "/service/*"
                    + descriptor.getPathPattern(), descriptor.getFilterExpression() );

                // TODO: recheck this? We are adding a flat wall to be hit if a mapping is missed
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration )
                    .addProtectedResource(
                        "/service/**",
                        "authcBasic,perms[nexus:someFreakinStupidPermToCatchAllUnprotectedsAndOnlyAdminWillHaveItSinceItHaveAStar]" );
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to protect resource mounted to "
                    + resource.getResourceUri() + " of class " + resource.getClass().getName(), e );
            }
        }
    }
}

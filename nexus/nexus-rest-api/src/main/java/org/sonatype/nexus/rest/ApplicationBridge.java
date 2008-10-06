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
import org.restlet.Router;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.rest.artifact.ArtifactResourceContentHandler;
import org.sonatype.nexus.rest.artifact.ArtifactResourceHandler;
import org.sonatype.nexus.rest.artifact.ArtifactResourceRedirectHandler;
import org.sonatype.nexus.rest.attributes.AttributesResourceHandler;
import org.sonatype.nexus.rest.authentication.LoginResourceHandler;
import org.sonatype.nexus.rest.authentication.LogoutResourceHandler;
import org.sonatype.nexus.rest.cache.CacheResourceHandler;
import org.sonatype.nexus.rest.identify.IdentifyHashResourceHandler;
import org.sonatype.nexus.rest.index.IndexResourceHandler;
import org.sonatype.nexus.rest.status.CommandResourceHandler;
import org.sonatype.nexus.rest.status.StatusResourceHandler;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

import com.thoughtworks.xstream.XStream;

/**
 * Nexus REST bridge. We have the needed Nexus specific customizations made here, such as creation and configuration of
 * shared XStream instance and creating Application root.
 * 
 * @author cstamas
 */
@Component( role = Application.class, hint = "service" )
public class ApplicationBridge
    extends PlexusRestletApplicationBridge
    implements EventListener
{
    @Requirement
    private Nexus nexus;

    @Requirement( hint = "nexusInstance" )
    private Filter nexusInstanceFilter;

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
    protected void doConfigure()
    {
        nexus.getNexusConfiguration().addProximityEventListener( this );
    }

    /**
     * Configuring xstream with our aliases.
     */
    protected XStream doConfigureXstream( XStream xstream )
    {
        return XStreamInitializer.initialize( xstream );
    }

    protected Router initializeRouter( Router root )
    {
        // instance filter, that injects proper Nexus instance into request attributes
        nexusInstanceFilter.setContext( getContext() );

        // attaching filter to a root on given URI
        attach( root, false, "/{" + NexusInstanceFilter.NEXUS_INSTANCE_KEY + "}", nexusInstanceFilter );

        // creating _another_ router, that will be next isntance called after filtering
        Router applicationRouter = new Router( getContext() );

        // attaching it after nif
        nexusInstanceFilter.setNext( applicationRouter );

        return applicationRouter;
    }

    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    protected void doCreateRoot( Router applicationRouter, boolean isStarted )
    {
        attach( applicationRouter, false, "/status", StatusResourceHandler.class );

        attach( applicationRouter, false, "/status/command", CommandResourceHandler.class );

        if ( !isStarted )
        {
            return;
        }

        // ==========================================================
        // now we are playing with the two router: unprotectedResources for not protected
        // and protectedResources for protected ones

        // attaching the restlets to scond router
        attach( applicationRouter, false, "/authentication/login", LoginResourceHandler.class );

        attach( applicationRouter, false, "/authentication/logout", LogoutResourceHandler.class );

        attach( applicationRouter, false, "/identify/{" + IdentifyHashResourceHandler.ALGORITHM_KEY + "}/{"
            + IdentifyHashResourceHandler.HASH_KEY + "}", IdentifyHashResourceHandler.class );

        attach( applicationRouter, false, "/artifact/maven", ArtifactResourceHandler.class );

        attach( applicationRouter, false, "/artifact/maven/redirect", ArtifactResourceRedirectHandler.class );

        attach( applicationRouter, false, "/artifact/maven/content", ArtifactResourceContentHandler.class );

        // protected resources

        attach( applicationRouter, false, "/data_index", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_index/{" + IndexResourceHandler.DOMAIN + "}/{"
            + IndexResourceHandler.TARGET_ID + "}", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_index/{" + IndexResourceHandler.DOMAIN + "}/{"
            + IndexResourceHandler.TARGET_ID + "}/content", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_cache/{" + CacheResourceHandler.DOMAIN + "}/{"
            + CacheResourceHandler.TARGET_ID + "}/content", CacheResourceHandler.class );

        attach( applicationRouter, false, "/attributes", AttributesResourceHandler.class );

        attach( applicationRouter, false, "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{"
            + AttributesResourceHandler.TARGET_ID + "}", AttributesResourceHandler.class );

        attach( applicationRouter, false, "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{"
            + AttributesResourceHandler.TARGET_ID + "}/content", AttributesResourceHandler.class );

        // attach( applicationRouter, false, "/repository_statuses", RepositoryStatusesListResourceHandler.class );

        // attach( applicationRouter, false, "/repositories", RepositoryListResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}",
        // RepositoryResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/status",
        // RepositoryStatusResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/meta",
        // RepositoryMetaResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/content",
        // RepositoryContentResourceHandler.class );

        // attach( applicationRouter, false, "/repo_groups", RepositoryGroupListResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}",
        // RepositoryGroupResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}/content",
        // RepositoryGroupContentResourceHandler.class );

        // attach( applicationRouter, false, "/global_settings", GlobalConfigurationListResourceHandler.class );

        // attach( applicationRouter, false, "/global_settings/{" + GlobalConfigurationResourceHandler.CONFIG_NAME_KEY
        // + "}", GlobalConfigurationResourceHandler.class );

        // attach( applicationRouter, false, "/repo_routes", RepositoryRouteListResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/repo_routes/{" + RepositoryRouteResourceHandler.ROUTE_ID_KEY + "}",
        // RepositoryRouteResourceHandler.class );

        // attach( applicationRouter, false, "/templates/repositories", RepositoryTemplateListResourceHandler.class );

        // attach( applicationRouter, false, "/templates/repositories/{"
        // + RepositoryTemplateResourceHandler.REPOSITORY_ID_KEY + "}", RepositoryTemplateResourceHandler.class );

        // attach( applicationRouter, false, "/schedules", ScheduledServiceListResourceHandler.class );

        // attach( applicationRouter, false, "/schedule_types", ScheduledServiceTypeResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/schedule_run/{" + ScheduledServiceRunResourceHandler.SCHEDULED_SERVICE_ID_KEY + "}",
        // ScheduledServiceRunResourceHandler.class );

        // attach( applicationRouter, false, "/schedules/{" + ScheduledServiceResourceHandler.SCHEDULED_SERVICE_ID_KEY
        // + "}", ScheduledServiceResourceHandler.class );

        // attach( applicationRouter, false, "/users", UserListResourceHandler.class );

        // attach( applicationRouter, false, "/users/{" + UserResourceHandler.USER_ID_KEY + "}",
        // UserResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/users_reset/{" + UserResourceHandler.USER_ID_KEY + "}",
        // UserResetResourceHandler.class );

        // attach(
        // applicationRouter,
        // false,
        // "/users_forgotid/{" + UserResourceHandler.USER_EMAIL_KEY + "}",
        // UserForgotIdResourceHandler.class );

        // attach( applicationRouter, false, "/users_forgotpw", UserForgotPasswordResourceHandler.class );

        // attach( applicationRouter, false, "/users_changepw", UserChangePasswordResourceHandler.class );

        // attach( applicationRouter, false, "/roles", RoleListResourceHandler.class );

        // attach( applicationRouter, false, "/roles/{" + RoleResourceHandler.ROLE_ID_KEY + "}",
        // RoleResourceHandler.class );

        // attach( applicationRouter, false, "/privileges", PrivilegeListResourceHandler.class );

    }
}

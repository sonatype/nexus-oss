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

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.resource.Resource;
import org.restlet.util.Template;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.NexusStartedEvent;
import org.sonatype.nexus.NexusStoppingEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.rest.artifact.ArtifactResourceContentHandler;
import org.sonatype.nexus.rest.artifact.ArtifactResourceHandler;
import org.sonatype.nexus.rest.artifact.ArtifactResourceRedirectHandler;
import org.sonatype.nexus.rest.attributes.AttributesResourceHandler;
import org.sonatype.nexus.rest.authentication.LoginResourceHandler;
import org.sonatype.nexus.rest.authentication.LogoutResourceHandler;
import org.sonatype.nexus.rest.cache.CacheResourceHandler;
import org.sonatype.nexus.rest.contentclasses.ContentClassesListResourceHandler;
import org.sonatype.nexus.rest.feeds.FeedResourceHandler;
import org.sonatype.nexus.rest.feeds.FeedsListResourceHandler;
import org.sonatype.nexus.rest.global.GlobalConfigurationListResourceHandler;
import org.sonatype.nexus.rest.global.GlobalConfigurationResourceHandler;
import org.sonatype.nexus.rest.groups.RepositoryGroupContentResourceHandler;
import org.sonatype.nexus.rest.groups.RepositoryGroupListResourceHandler;
import org.sonatype.nexus.rest.groups.RepositoryGroupResourceHandler;
import org.sonatype.nexus.rest.identify.IdentifyHashResourceHandler;
import org.sonatype.nexus.rest.index.IndexResourceHandler;
import org.sonatype.nexus.rest.logs.LogsListResourceHandler;
import org.sonatype.nexus.rest.logs.LogsResourceHandler;
import org.sonatype.nexus.rest.privileges.PrivilegeListResourceHandler;
import org.sonatype.nexus.rest.privileges.PrivilegeResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryContentResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryListResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryMetaResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryStatusResourceHandler;
import org.sonatype.nexus.rest.repositorystatuses.RepositoryStatusesListResourceHandler;
import org.sonatype.nexus.rest.repotargets.RepositoryTargetListResourceHandler;
import org.sonatype.nexus.rest.repotargets.RepositoryTargetResourceHandler;
import org.sonatype.nexus.rest.roles.RoleListResourceHandler;
import org.sonatype.nexus.rest.roles.RoleResourceHandler;
import org.sonatype.nexus.rest.routes.RepositoryRouteListResourceHandler;
import org.sonatype.nexus.rest.routes.RepositoryRouteResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceListResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceRunResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceTypeResourceHandler;
import org.sonatype.nexus.rest.status.CommandResourceHandler;
import org.sonatype.nexus.rest.status.StatusResourceHandler;
import org.sonatype.nexus.rest.templates.repositories.RepositoryTemplateListResourceHandler;
import org.sonatype.nexus.rest.templates.repositories.RepositoryTemplateResourceHandler;
import org.sonatype.nexus.rest.users.UserChangePasswordResourceHandler;
import org.sonatype.nexus.rest.users.UserForgotIdResourceHandler;
import org.sonatype.nexus.rest.users.UserForgotPasswordResourceHandler;
import org.sonatype.nexus.rest.users.UserListResourceHandler;
import org.sonatype.nexus.rest.users.UserResetResourceHandler;
import org.sonatype.nexus.rest.users.UserResourceHandler;
import org.sonatype.nexus.rest.wastebasket.WastebasketResourceHandler;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;

import com.thoughtworks.xstream.XStream;

/**
 * Nexus REST bridge. We have the needed Nexus specific customizations made here, such as creation and configuration of
 * shared XStream instance and creating Application root.
 * 
 * @author cstamas
 */
public class ApplicationBridge
    extends PlexusRestletApplicationBridge
    implements ConfigurationChangeListener
{
    private Nexus nexus;

    /**
     * Constructor to enable usage in ServletRestletApplicationBridge.
     * 
     * @param context
     */
    public ApplicationBridge( Context context )
    {
        super( context );
    }

    /**
     * ConfigurationChangeListener.
     */
    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        if ( NexusStartedEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( true );
        }
        else if ( NexusStoppingEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( false );
        }
    }

    /**
     * Lookups Nexus instance in this plexus.
     * 
     * @return
     * @deprecated
     */
    protected Nexus getNexus()
    {
        if ( nexus == null )
        {
            try
            {
                nexus = (Nexus) getPlexusContainer().lookup( Nexus.ROLE );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot get Nexus instance!", e );
            }
        }

        return nexus;
    }

    /**
     * Adding this as config change listener.
     */
    protected void doConfigure()
    {
        getNexus().getNexusConfiguration().addConfigurationChangeListener( this );
    }

    /**
     * Configuring xstream with our aliases.
     */
    protected XStream doConfigureXstream( XStream xstream )
    {
        return XStreamInitializer.initialize( xstream );
    }

    /*
    protected Router initializeRouter( Router root )
    {
        // instance filter, that injects proper Nexus instance into request attributes
        NexusInstanceFilter nif = new NexusInstanceFilter( getContext() );

        // attaching filter to a root on given URI
        attach( root, false, "/{" + NexusInstanceFilter.NEXUS_INSTANCE_KEY + "}", nif );

        // creating _another_ router, that will be next isntance called after filtering
        Router applicationRouter = new Router( getContext() );
               
        // attaching it after nif
        nif.setNext( applicationRouter );
        
        return applicationRouter;
    }
    */
    
    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    @SuppressWarnings("deprecation")
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        // instance filter, that injects proper Nexus instance into request attributes
        NexusInstanceFilter nif = new NexusInstanceFilter( getContext() );

        // attaching filter to a root on given URI
        attach( root, false, "/{" + NexusInstanceFilter.NEXUS_INSTANCE_KEY + "}", nif );

        // creating _another_ router, that will be next isntance called after filtering
        Router applicationRouter = new Router( getContext() );
               
        // attaching it after nif
        nif.setNext( applicationRouter );                
        
        // a little digression here. if !isStarted, we are shutting down everything except /status and /status/command

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
        attach( applicationRouter, false, "/feeds", FeedsListResourceHandler.class );

        attach( applicationRouter, false, "/feeds/{" + FeedResourceHandler.FEED_KEY + "}", FeedResourceHandler.class );

        attach( applicationRouter, false, "/authentication/login", LoginResourceHandler.class );

        attach( applicationRouter, false, "/authentication/logout", LogoutResourceHandler.class );

        attach( applicationRouter, false, "/identify/{" + IdentifyHashResourceHandler.ALGORITHM_KEY + "}/{"
            + IdentifyHashResourceHandler.HASH_KEY + "}", IdentifyHashResourceHandler.class );

        attach( applicationRouter, false, "/artifact/maven", ArtifactResourceHandler.class );

        attach( applicationRouter, false, "/artifact/maven/redirect", ArtifactResourceRedirectHandler.class );

        attach( applicationRouter, false, "/artifact/maven/content", ArtifactResourceContentHandler.class );

        // protected resources

        attach( applicationRouter, false, "/data_index", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_index/{" + IndexResourceHandler.DOMAIN + "}/{" + IndexResourceHandler.TARGET_ID
            + "}", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_index/{" + IndexResourceHandler.DOMAIN + "}/{" + IndexResourceHandler.TARGET_ID
            + "}/content", IndexResourceHandler.class );

        attach( applicationRouter, false, "/data_cache/{" + CacheResourceHandler.DOMAIN + "}/{" + CacheResourceHandler.TARGET_ID
            + "}/content", CacheResourceHandler.class );

        attach( applicationRouter, false, "/wastebasket", WastebasketResourceHandler.class );

        attach( applicationRouter, false, "/attributes", AttributesResourceHandler.class );

        attach( applicationRouter, false, "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{"
            + AttributesResourceHandler.TARGET_ID + "}", AttributesResourceHandler.class );

        attach( applicationRouter, false, "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{"
            + AttributesResourceHandler.TARGET_ID + "}/content", AttributesResourceHandler.class );

        attach( applicationRouter, false, "/repository_statuses", RepositoryStatusesListResourceHandler.class );

        attach( applicationRouter, false, "/repositories", RepositoryListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}",
            RepositoryResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/status",
            RepositoryStatusResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/meta",
            RepositoryMetaResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/content",
            RepositoryContentResourceHandler.class );

        attach( applicationRouter, false, "/repo_groups", RepositoryGroupListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}",
            RepositoryGroupResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}/content",
            RepositoryGroupContentResourceHandler.class );

        attach( applicationRouter, false, "/logs", LogsListResourceHandler.class );

        attach( applicationRouter, false, "/logs/{" + LogsResourceHandler.FILE_NAME_KEY + "}", LogsResourceHandler.class );

        attach( applicationRouter, false, "/global_settings", GlobalConfigurationListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/global_settings/{" + GlobalConfigurationResourceHandler.CONFIG_NAME_KEY + "}",
            GlobalConfigurationResourceHandler.class );

        attach( applicationRouter, false, "/repo_routes", RepositoryRouteListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repo_routes/{" + RepositoryRouteResourceHandler.ROUTE_ID_KEY + "}",
            RepositoryRouteResourceHandler.class );

        attach( applicationRouter, false, "/templates/repositories", RepositoryTemplateListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/templates/repositories/{" + RepositoryTemplateResourceHandler.REPOSITORY_ID_KEY + "}",
            RepositoryTemplateResourceHandler.class );

        attach( applicationRouter, false, "/schedules", ScheduledServiceListResourceHandler.class );

        attach( applicationRouter, false, "/schedule_types", ScheduledServiceTypeResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/schedule_run/{" + ScheduledServiceRunResourceHandler.SCHEDULED_SERVICE_ID_KEY + "}",
            ScheduledServiceRunResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/schedules/{" + ScheduledServiceResourceHandler.SCHEDULED_SERVICE_ID_KEY + "}",
            ScheduledServiceResourceHandler.class );

        attach( applicationRouter, false, "/users", UserListResourceHandler.class );

        attach( applicationRouter, false, "/users/{" + UserResourceHandler.USER_ID_KEY + "}", UserResourceHandler.class );

        attach( applicationRouter, false, "/users_reset/{" + UserResourceHandler.USER_ID_KEY + "}", UserResetResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/users_forgotid/{" + UserResourceHandler.USER_EMAIL_KEY + "}",
            UserForgotIdResourceHandler.class );

        attach( applicationRouter, false, "/users_forgotpw", UserForgotPasswordResourceHandler.class );

        attach( applicationRouter, false, "/users_changepw", UserChangePasswordResourceHandler.class );

        attach( applicationRouter, false, "/roles", RoleListResourceHandler.class );

        attach( applicationRouter, false, "/roles/{" + RoleResourceHandler.ROLE_ID_KEY + "}", RoleResourceHandler.class );

        attach( applicationRouter, false, "/privileges", PrivilegeListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/privileges/{" + PrivilegeResourceHandler.PRIVILEGE_ID_KEY + "}",
            PrivilegeResourceHandler.class );

        attach( applicationRouter, false, "/repo_targets", RepositoryTargetListResourceHandler.class );

        attach(
            applicationRouter,
            false,
            "/repo_targets/{" + RepositoryTargetResourceHandler.REPO_TARGET_ID_KEY + "}",
            RepositoryTargetResourceHandler.class );

        attach( applicationRouter, false, "/repo_content_classes", ContentClassesListResourceHandler.class );
    }
}

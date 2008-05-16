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

import java.util.Date;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.resource.Resource;
import org.sonatype.nexus.rest.artifact.ArtifactResourceContentHandler;
import org.sonatype.nexus.rest.artifact.ArtifactResourceHandler;
import org.sonatype.nexus.rest.attributes.AttributesResourceHandler;
import org.sonatype.nexus.rest.authentication.LoginResourceHandler;
import org.sonatype.nexus.rest.authentication.LogoutResourceHandler;
import org.sonatype.nexus.rest.cache.CacheResourceHandler;
import org.sonatype.nexus.rest.configurations.ConfigurationsListResourceHandler;
import org.sonatype.nexus.rest.configurations.ConfigurationsResourceHandler;
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
import org.sonatype.nexus.rest.repositories.RepositoryContentResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryListResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryMetaResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryResourceHandler;
import org.sonatype.nexus.rest.repositories.RepositoryStatusResourceHandler;
import org.sonatype.nexus.rest.repositorystatuses.RepositoryStatusesListResourceHandler;
import org.sonatype.nexus.rest.routes.RepositoryRouteListResourceHandler;
import org.sonatype.nexus.rest.routes.RepositoryRouteResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceListResourceHandler;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceHandler;
import org.sonatype.nexus.rest.status.StatusResourceHandler;
import org.sonatype.nexus.rest.templates.repositories.RepositoryTemplateListResourceHandler;
import org.sonatype.nexus.rest.templates.repositories.RepositoryTemplateResourceHandler;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.security.SimpleAuthenticationSource;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.RestletOrgApplication;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Nexus REST bridge. We have the needed Nexus specific customizations made here, such as creation and configuration of
 * shared XStream instance and creating Application root.
 * 
 * @author cstamas
 */
public class ApplicationBridge
    extends PlexusRestletApplicationBridge
    implements RestletOrgApplication
{

    /** Key to store JSON driver driven XStream */
    public static final String JSON_XSTREAM = "nexus.xstream.json";

    /** Key to store XML driver driven XStream */
    public static final String XML_XSTREAM = "nexus.xstream.xml";

    /** Key to store used Commons Fileupload FileItemFactory */
    public static final String FILEITEM_FACTORY = "nexus.fileItemFactory";

    public final Date createdOn;

    /**
     * Constructor to enable usage in ServletRestletApplicationBridge.
     * 
     * @param context
     */
    public ApplicationBridge( Context context )
    {
        super( context );

        this.createdOn = new Date();
    }

    /**
     * Returns the timestamp of instantaniation of this object. This is used as timestamp for transient objects when
     * they are still unchanged (not modified).
     * 
     * @return date
     */
    public Date getCreatedOn()
    {
        return createdOn;
    }

    /**
     * Creating all sort of shared tools and putting them into context, to make them usable by per-request
     * instantaniated Resource implementors.
     */
    protected void configure()
    {
        // we are putting XStream into this Application's Context, since XStream is threadsafe
        // and it is safe to share it across multiple threads. XStream is heavily used by our
        // custom Representation implementation to support XML and JSON.

        // create and configure XStream for JSON
        XStream xstream = createAndConfigureXstream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xstream.registerConverter( new PrimitiveKeyedMapConverter( xstream.getMapper() ) );

        // put it into context
        getContext().getAttributes().put( JSON_XSTREAM, xstream );

        // create and configure XStream for XML
        xstream = createAndConfigureXstream( new DomDriver() );

        // put it into context
        getContext().getAttributes().put( XML_XSTREAM, xstream );

        // put fileItemFactory into context
        getContext().getAttributes().put( FILEITEM_FACTORY, new DiskFileItemFactory() );
    }

    /**
     * Creates and configures XStream instance for Nexus needs.
     * 
     * @param driver
     * @return
     */
    protected XStream createAndConfigureXstream( HierarchicalStreamDriver driver )
    {
        return XStreamInitializer.initialize( new XStream( driver ) );
    }

    /**
     * Creating restlet application root.
     */
    public Restlet createRoot()
    {
        configure();

        // TODO: this has to be externalized somehow

        // this is 100% what would you do in pure restlet.org Application createRoot() method.

        // The root is a router
        Router root = new Router( getContext() );

        // instance filter, that injects proper Nexus instance into request attributes
        NexusInstanceFilter nif = new NexusInstanceFilter( getContext() );

        // attaching filter to a root on given URI
        root.attach( "/{" + NexusInstanceFilter.NEXUS_INSTANCE_KEY + "}", nif );

        // simple anonymous guard, that injects anonymous users for all reqs. later, the protected
        // resources will do _real_ auth if needed.
        NexusAnonymousGuard nexusGuard = new NexusAnonymousGuard( getContext() );

        // attaching it after nif
        nif.setNext( nexusGuard );

        // creating _another_ router, that will be next isntance called after filtering
        Router router = new Router( getContext() );

        // setting the new router to be next instance
        nexusGuard.setNext( router );

        // ==========================================================
        // now we are playing with the two router: unprotectedResources for not protected
        // and protectedResources for protected ones

        // attaching the restlets to scond router
        router.attach( "/status", StatusResourceHandler.class );

        router.attach( "/artifact/maven", ArtifactResourceHandler.class );

        router.attach( "/artifact/maven/content", ArtifactResourceContentHandler.class );

        router.attach( "/data_index", IndexResourceHandler.class );

        router.attach( "/data_index/{" + IndexResourceHandler.DOMAIN + "}/{" + IndexResourceHandler.TARGET_ID
            + "}/content", IndexResourceHandler.class );

        router.attach( "/attributes", AttributesResourceHandler.class );

        router.attach( "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{" + AttributesResourceHandler.TARGET_ID
            + "}/content", AttributesResourceHandler.class );

        router.attach( "/feeds", FeedsListResourceHandler.class );

        router.attach( "/feeds/{" + FeedResourceHandler.FEED_KEY + "}", FeedResourceHandler.class );

        router.attach(
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/content",
            RepositoryContentResourceHandler.class );

        router.attach( "/repository_statuses", RepositoryStatusesListResourceHandler.class );

        router.attach(
            "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}/content",
            RepositoryGroupContentResourceHandler.class );

        router.attach( "/authentication/logout", LogoutResourceHandler.class );

        router.attach( "/identify/{" + IdentifyHashResourceHandler.ALGORITHM_KEY + "}/{"
            + IdentifyHashResourceHandler.HASH_KEY + "}", IdentifyHashResourceHandler.class );

        // protected resources

        router.attach( "/authentication/login", protectResource( LoginResourceHandler.class ) );

        router.attach( "/logs", protectResource( LogsListResourceHandler.class ) );

        router
            .attach( "/logs/{" + LogsResourceHandler.FILE_NAME_KEY + "}", protectResource( LogsResourceHandler.class ) );

        router.attach( "/configs", protectResource( ConfigurationsListResourceHandler.class ) );

        router.attach(
            "/configs/{" + GlobalConfigurationResourceHandler.CONFIG_NAME_KEY + "}",
            protectResource( ConfigurationsResourceHandler.class ) );

        router.attach( "/global_settings", protectResource( GlobalConfigurationListResourceHandler.class ) );

        router.attach(
            "/global_settings/{" + GlobalConfigurationResourceHandler.CONFIG_NAME_KEY + "}",
            protectResource( GlobalConfigurationResourceHandler.class ) );

        router.attach( "/repositories", protectWriteToResource( RepositoryListResourceHandler.class ) );

        router.attach(
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}",
            protectResource( RepositoryResourceHandler.class ) );

        router.attach(
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/status",
            protectResource( RepositoryStatusResourceHandler.class ) );

        router.attach(
            "/repositories/{" + RepositoryResourceHandler.REPOSITORY_ID_KEY + "}/meta",
            protectResource( RepositoryMetaResourceHandler.class ) );

        router.attach( "/repo_groups", protectResource( RepositoryGroupListResourceHandler.class ) );

        router.attach(
            "/repo_groups/{" + RepositoryGroupResourceHandler.GROUP_ID_KEY + "}",
            protectResource( RepositoryGroupResourceHandler.class ) );

        router.attach( "/repo_routes", protectResource( RepositoryRouteListResourceHandler.class ) );

        router.attach(
            "/repo_routes/{" + RepositoryRouteResourceHandler.ROUTE_ID_KEY + "}",
            protectResource( RepositoryRouteResourceHandler.class ) );

        router.attach( "/templates/repositories", protectResource( RepositoryTemplateListResourceHandler.class ) );

        router.attach(
            "/templates/repositories/{" + RepositoryTemplateResourceHandler.REPOSITORY_ID_KEY + "}",
            protectResource( RepositoryTemplateResourceHandler.class ) );

        router.attach( "/data_cache/{" + CacheResourceHandler.DOMAIN + "}/{" + CacheResourceHandler.TARGET_ID
            + "}/content", protectResource( CacheResourceHandler.class ) );
        
        router.attach( "/schedules", protectResource( ScheduledServiceListResourceHandler.class ) );
        
        router.attach( 
            "/schedules/{" + ScheduledServiceResourceHandler.SCHEDULED_SERVICE_ID_KEY + "}",
            protectResource( ScheduledServiceResourceHandler.class ) );

        // returning root
        return root;
    }

    protected NexusAuthenticationGuard protectResource( Class<? extends Resource> targetClass )
    {
        NexusAuthenticationGuard result = new NexusAuthenticationGuard(
            getContext(),
            SimpleAuthenticationSource.ADMIN_USERNAME );

        result.setNext( targetClass );

        return result;
    }

    protected NexusAuthenticationGuard protectWriteToResource( Class<? extends Resource> targetClass )
    {
        NexusWriteAccessAuthenticationGuard result = new NexusWriteAccessAuthenticationGuard(
            getContext(),
            SimpleAuthenticationSource.ADMIN_USERNAME );

        result.setNext( targetClass );

        return result;
    }
}

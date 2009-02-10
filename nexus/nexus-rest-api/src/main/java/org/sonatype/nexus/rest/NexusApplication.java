/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest;

import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Scm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Router;
import org.sonatype.jsecurity.web.PlexusMutableWebConfiguration;
import org.sonatype.jsecurity.web.PlexusWebConfiguration;
import org.sonatype.jsecurity.web.SecurityConfigurationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.rest.model.*;
import org.sonatype.nexus.rest.repositories.RepositoryBaseResourceConverter;
import org.sonatype.nexus.rest.repositories.RepositoryResourceResponseConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServicePropertyResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceResponseConverter;
import org.sonatype.plexus.rest.PlexusResourceFinder;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

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

    @Requirement( hint = "indexTemplate" )
    private ManagedPlexusResource indexTemplateResource;

    @Requirement( hint = "IndexRedirectingPlexusResource" )
    private ManagedPlexusResource indexRedirectingResource;

    @Requirement( hint = "content" )
    private ManagedPlexusResource contentResource;

    @Requirement( hint = "StatusPlexusResource" )
    private ManagedPlexusResource statusPlexusResource;

    @Requirement( hint = "CommandPlexusResource" )
    private ManagedPlexusResource commandPlexusResource;

    @Requirement( role = NexusResourceBundle.class )
    private List<NexusResourceBundle> nexusResourceBundles;

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
        // adding ourselves as listener
        nexus.getNexusConfiguration().addProximityEventListener( this );
    }

    /**
     * Configuring xstream with our aliases.
     */
    @Override
    public XStream doConfigureXstream( XStream xstream )
    {
        xstream.registerConverter( new RepositoryBaseResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new RepositoryResourceResponseConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from
        // data

        xstream.registerConverter( new ScheduledServiceBaseResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new ScheduledServicePropertyResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new ScheduledServiceResourceResponseConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from
        // data

        // Maven POM
        xstream.alias( "project", Model.class );

        // omitting modelEncoding
        xstream.omitField( NexusErrorResponse.class, "modelEncoding" );
        // xstream.addImplicitCollection( NexusErrorResponse.class, "errors", "error", NexusError.class ); // FIXME:
        // this might break the JSON parser, test it before checking in
        xstream.omitField( NexusError.class, "modelEncoding" );
        xstream.alias( "nexus-error", NexusErrorResponse.class );
        xstream.alias( "error", NexusError.class );
        xstream.registerLocalConverter( NexusErrorResponse.class, "errors", new AliasingListConverter(
            NexusError.class,
            "error" ) );

        xstream.omitField( ContentListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ContentListResource.class, "modelEncoding" );
        xstream.alias( "content", ContentListResourceResponse.class );
        xstream.alias( "content-item", ContentListResource.class );
        xstream.registerLocalConverter( ContentListResourceResponse.class, "data", new AliasingListConverter(
            ContentListResource.class,
            "content-item" ) );

        xstream.omitField( RepositoryResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryBaseResource.class, "modelEncoding" );
        xstream.omitField( RepositoryResource.class, "modelEncoding" );
        xstream.omitField( RepositoryProxyResource.class, "modelEncoding" );
        xstream.omitField( RepositoryShadowResource.class, "modelEncoding" );
        xstream.omitField( RepositoryResourceRemoteStorage.class, "modelEncoding" );
        xstream.alias( "repository", RepositoryResourceResponse.class );

        xstream.omitField( RepositoryListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryListResource.class, "modelEncoding" );
        xstream.alias( "repositories", RepositoryListResourceResponse.class );
        // xstream.alias( "repositories-item", RepositoryListResource.class);
        xstream.registerLocalConverter( RepositoryListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryListResource.class,
            "repositories-item" ) );

        xstream.omitField( RepositoryStatusResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusResource.class, "modelEncoding" );
        xstream.alias( "repository-status", RepositoryStatusResourceResponse.class );

        xstream.omitField( RepositoryStatusListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusListResource.class, "modelEncoding" );
        xstream.alias( "repository-status-list", RepositoryStatusListResourceResponse.class );
        // xstream.alias( "repository-status-list-item", RepositoryStatusListResource.class);
        xstream.registerLocalConverter( RepositoryStatusListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryStatusListResource.class,
            "repository-status-list-item" ) );

        xstream.omitField( RepositoryMetaResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryMetaResource.class, "modelEncoding" );
        xstream.alias( "repository-meta-data", RepositoryMetaResourceResponse.class );

        xstream.omitField( RepositoryGroupListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupListResource.class, "modelEncoding" );
        xstream.alias( "repo-group-list", RepositoryGroupListResourceResponse.class );
        // xstream.alias( "repo-group-list-item", RepositoryGroupListResource.class);
        // xstream.alias( "repo-group-memeber", RepositoryGroupMemberRepository.class);
        xstream.registerLocalConverter( RepositoryGroupListResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class,
            "repo-group-memeber" ) );
        xstream.registerLocalConverter( RepositoryGroupResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class,
            "repo-group-memeber" ) );
        xstream.registerLocalConverter( RepositoryGroupListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryGroupListResource.class,
            "repo-group-list-item" ) );

        xstream.omitField( RepositoryGroupResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupResource.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-group", RepositoryGroupResourceResponse.class );

        xstream.omitField( RepositoryRouteListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteListResource.class, "modelEncoding" );
        xstream.alias( "repo-routes-list", RepositoryRouteListResourceResponse.class );
        // xstream.alias( "repo-routes-list-item", RepositoryRouteListResource.class);
        // xstream.alias( "repo-routes-member", RepositoryRouteMemberRepository.class);
        xstream.registerLocalConverter( RepositoryRouteListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryRouteListResource.class,
            "repo-routes-list-item" ) );
        xstream.registerLocalConverter( RepositoryRouteListResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class,
            "repo-routes-member" ) );

        xstream.omitField( RepositoryRouteResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteResource.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-route", RepositoryRouteResourceResponse.class );
        xstream.registerLocalConverter( RepositoryRouteResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class,
            "repository" ) );

        xstream.omitField( GlobalConfigurationListResourceResponse.class, "modelEncoding" );
        xstream.omitField( GlobalConfigurationListResource.class, "modelEncoding" );
        xstream.alias( "global-settings-list", GlobalConfigurationListResourceResponse.class );
        // xstream.alias( "global-settings-list-item", GlobalConfigurationListResource.class);
        xstream.registerLocalConverter(
            GlobalConfigurationListResourceResponse.class,
            "data",
            new AliasingListConverter( GlobalConfigurationListResource.class, "global-settings-list-item" ) );

        xstream.omitField( GlobalConfigurationResourceResponse.class, "modelEncoding" );
        xstream.omitField( GlobalConfigurationResource.class, "modelEncoding" );
        xstream.omitField( RemoteConnectionSettings.class, "modelEncoding" );
        xstream.omitField( RemoteHttpProxySettings.class, "modelEncoding" );
        xstream.omitField( AuthenticationSettings.class, "modelEncoding" );
        xstream.omitField( SmtpSettings.class, "modelEncoding" );
        xstream.alias( "global-settings", GlobalConfigurationResourceResponse.class );

        xstream.omitField( WastebasketResource.class, "modelEncoding" );
        xstream.omitField( WastebasketResourceResponse.class, "modelEncoding" );
        xstream.alias( "wastebasket", WastebasketResourceResponse.class );

        xstream.omitField( LogsListResourceResponse.class, "modelEncoding" );
        xstream.omitField( LogsListResource.class, "modelEncoding" );
        xstream.alias( "logs-list", LogsListResourceResponse.class );
        // xstream.alias( "logs-list-item", LogsListResource.class);
        xstream.registerLocalConverter( LogsListResourceResponse.class, "data", new AliasingListConverter(
            LogsListResource.class,
            "logs-list-item" ) );

        xstream.omitField( ConfigurationsListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ConfigurationsListResource.class, "modelEncoding" );
        xstream.alias( "configs-list", ConfigurationsListResourceResponse.class );
        // xstream.alias( "configs-list-tem", ConfigurationsListResource.class);
        xstream.registerLocalConverter( ConfigurationsListResourceResponse.class, "data", new AliasingListConverter(
            ConfigurationsListResource.class,
            "configs-list-tem" ) );

        xstream.omitField( FeedListResourceResponse.class, "modelEncoding" );
        xstream.omitField( FeedListResource.class, "modelEncoding" );
        xstream.alias( "feeds-list", FeedListResourceResponse.class );
        // xstream.alias( "feeds-list-item", FeedListResource.class);
        xstream.registerLocalConverter( FeedListResourceResponse.class, "data", new AliasingListConverter(
            FeedListResource.class,
            "feeds-list-item" ) );

        xstream.omitField( SearchResponse.class, "modelEncoding" );
        xstream.alias( "search-results", SearchResponse.class );
        xstream.registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter(
            NexusArtifact.class,
            "artifact" ) );

        xstream.omitField( NexusResponse.class, "modelEncoding" );
        xstream.omitField( NexusArtifact.class, "modelEncoding" );
        xstream.alias( "artifact", NexusArtifact.class );

        xstream.omitField( AuthenticationLoginResourceResponse.class, "modelEncoding" );
        xstream.omitField( AuthenticationLoginResource.class, "modelEncoding" );
        xstream.omitField( AuthenticationClientPermissions.class, "modelEncoding" );
        xstream.alias( "authentication-login", AuthenticationLoginResourceResponse.class );

        xstream.omitField( StatusResource.class, "modelEncoding" );
        xstream.omitField( StatusResourceResponse.class, "modelEncoding" );
        xstream.omitField( StatusConfigurationValidationResponse.class, "modelEncoding" );
        xstream.alias( "status", StatusResourceResponse.class );
        xstream.registerLocalConverter(
            StatusConfigurationValidationResponse.class,
            "validationErrors",
            new AliasingListConverter( String.class, "error" ) );
        xstream.registerLocalConverter(
            StatusConfigurationValidationResponse.class,
            "validationWarnings",
            new AliasingListConverter( String.class, "warning" ) );

        xstream.omitField( ScheduledServiceListResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceBaseResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServicePropertyResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceOnceResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceDailyResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceAdvancedResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceMonthlyResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceWeeklyResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceResourceResponse.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceTypeResourceResponse.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceTypeResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceTypePropertyResource.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceResourceStatus.class, "modelEncoding" );
        xstream.omitField( ScheduledServiceResourceStatusResponse.class, "modelEncoding" );
        xstream.alias( "schedules-list", ScheduledServiceListResourceResponse.class );
        // xstream.alias( "schedules-list-item", ScheduledServiceListResource.class );
        xstream.alias( "scheduled-task", ScheduledServiceResourceResponse.class );
        // xstream.alias( "scheduled-task-property", ScheduledServicePropertyResource.class );
        xstream.alias( "schedule-types", ScheduledServiceTypeResourceResponse.class );
        xstream.alias( "schedule-type", ScheduledServiceTypeResource.class );
        // xstream.alias( "schedule-type-property", ScheduledServiceTypePropertyResource.class );
        xstream.registerLocalConverter( ScheduledServiceBaseResource.class, "properties", new AliasingListConverter(
            ScheduledServicePropertyResource.class,
            "scheduled-task-property" ) );
        xstream.registerLocalConverter(
            ScheduledServiceWeeklyResource.class,
            "recurringDay",
            new AliasingListConverter( String.class, "day" ) );
        xstream.registerLocalConverter( ScheduledServiceTypeResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceTypeResource.class,
            "schedule-type" ) );
        xstream.registerLocalConverter( ScheduledServiceTypeResource.class, "properties", new AliasingListConverter(
            ScheduledServiceTypePropertyResource.class,
            "scheduled-task-property" ) );
        xstream.registerLocalConverter( ScheduledServiceListResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceListResource.class,
            "schedules-list-item" ) );

        xstream.omitField( UserListResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResourceRequest.class, "modelEncoding" );
        xstream.omitField( UserResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResource.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordResource.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordResource.class, "modelEncoding" );
        xstream.alias( "users-list", UserListResourceResponse.class );
        // xstream.alias( "users-list-item", UserResource.class );
        xstream.alias( "user-request", UserResourceRequest.class );
        xstream.alias( "user-response", UserResourceResponse.class );
        xstream.alias( "user-forgotpw", UserForgotPasswordRequest.class );
        xstream.alias( "user-changepw", UserChangePasswordRequest.class );
        xstream.registerLocalConverter( UserResource.class, "roles", new AliasingListConverter( String.class, "role" ) );
        xstream.registerLocalConverter( UserListResourceResponse.class, "data", new AliasingListConverter(
            UserResource.class,
            "users-list-item" ) );

        xstream.omitField( RoleListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RoleResource.class, "modelEncoding" );
        xstream.omitField( RoleResourceRequest.class, "modelEncoding" );
        xstream.omitField( RoleResourceResponse.class, "modelEncoding" );
        xstream.alias( "roles-list", RoleListResourceResponse.class );
        // xstream.alias( "roles-list-item", RoleResource.class );
        xstream.alias( "role-request", RoleResourceRequest.class );
        xstream.alias( "role-response", RoleResourceResponse.class );
        xstream.registerLocalConverter( RoleListResourceResponse.class, "data", new AliasingListConverter(
            RoleResource.class,
            "roles-list-item" ) );
        xstream.registerLocalConverter( RoleResource.class, "roles", new AliasingListConverter( String.class, "role" ) );
        xstream.registerLocalConverter( RoleResource.class, "privileges", new AliasingListConverter(
            String.class,
            "privilege" ) );

        xstream.omitField( PrivilegeResourceRequest.class, "modelEncoding" );
        xstream.omitField( PrivilegeResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeStatusResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeListResourceResponse.class, "modelEncoding" );
        xstream.omitField( PrivilegeStatusResourceResponse.class, "modelEncoding" );
        xstream.omitField( PrivilegeProperty.class, "modelEncoding" );
        xstream.omitField( PrivilegeTypeResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeTypePropertyResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeTypeResourceResponse.class, "modelEncoding" );
        xstream.alias( "privilege-request", PrivilegeResourceRequest.class );
        xstream.alias( "privilege-list-response", PrivilegeListResourceResponse.class );
        xstream.alias( "privilege-status-response", PrivilegeStatusResourceResponse.class );
        xstream.alias( "privilege-type-response", PrivilegeTypeResourceResponse.class );
        xstream.aliasField( "methods", PrivilegeResource.class, "method" );
        xstream.registerLocalConverter( PrivilegeListResourceResponse.class, "data", new AliasingListConverter(
            PrivilegeStatusResource.class,
            "privilege-item" ) );
        xstream.registerLocalConverter( PrivilegeResource.class, "method", new AliasingListConverter(
            String.class,
            "method" ) );
        xstream.registerLocalConverter( PrivilegeStatusResource.class, "properties", new AliasingListConverter(
            PrivilegeProperty.class,
            "privilege-property" ) );
        xstream.registerLocalConverter( PrivilegeTypeResourceResponse.class, "data", new AliasingListConverter(
            PrivilegeTypeResource.class,
            "privilege-type" ) );
        xstream.registerLocalConverter( PrivilegeTypeResource.class, "properties", new AliasingListConverter(
            PrivilegeTypePropertyResource.class,
            "privilege-type-property" ) );

        xstream.omitField( NFCResourceResponse.class, "modelEncoding" );
        xstream.omitField( NFCResource.class, "modelEncoding" );
        xstream.omitField( NFCRepositoryResource.class, "modelEncoding" );
        xstream.alias( "nfc-info", NFCResourceResponse.class );
        // xstream.alias( "nfc-repo-info", NFCRepositoryResource.class );
        xstream.registerLocalConverter( NFCResource.class, "nfcContents", new AliasingListConverter(
            NFCRepositoryResource.class,
            "nfc-repo-info" ) );
        xstream.registerLocalConverter( NFCRepositoryResource.class, "nfcPaths", new AliasingListConverter(
            String.class,
            "path" ) );

        xstream.omitField( RepositoryTargetListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetListResource.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResource.class, "modelEncoding" );
        xstream.alias( "repo-targets-list", RepositoryTargetListResourceResponse.class );
        // xstream.alias( "repo-targets-list-item", RepositoryTargetListResource.class );
        xstream.alias( "repo-target", RepositoryTargetResourceResponse.class );
        xstream.registerLocalConverter( RepositoryTargetResource.class, "patterns", new AliasingListConverter(
            String.class,
            "pattern" ) );
        xstream.registerLocalConverter( RepositoryTargetListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryTargetListResource.class,
            "repo-targets-list-item" ) );

        xstream.omitField( RepositoryContentClassListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryContentClassListResource.class, "modelEncoding" );
        xstream.alias( "repo-content-classes-list", RepositoryContentClassListResourceResponse.class );
        // xstream.alias( "repo-content-classes-list-item", RepositoryContentClassListResource.class );
        xstream.registerLocalConverter(
            RepositoryContentClassListResourceResponse.class,
            "data",
            new AliasingListConverter( RepositoryContentClassListResource.class, "repo-content-classes-list-item" ) );

        xstream.omitField( PlexusComponentListResourceResponse.class, "modelEncoding" );
        xstream.omitField( PlexusComponentListResource.class, "modelEncoding" );
        xstream.alias( "components-list", PlexusComponentListResourceResponse.class );
        xstream.alias( "component", PlexusComponentListResource.class );
        xstream.registerLocalConverter( PlexusComponentListResourceResponse.class, "data", new AliasingListConverter(
            PlexusComponentListResource.class,
            "component" ) );

        xstream.registerLocalConverter(
            AuthenticationClientPermissions.class,
            "permissions",
            new AliasingListConverter( ClientPermission.class, "permission" ) );

        xstream.omitField( ClientPermission.class, "modelEncoding" );

        xstream.omitField( UserToRoleResourceRequest.class, "modelEncoding" );
        xstream.omitField( UserToRoleResource.class, "modelEncoding" );
        xstream.alias( "user-to-role", UserToRoleResourceRequest.class );

        xstream.omitField( PlexusUserResourceResponse.class, "modelEncoding" );
        xstream.alias( "plexus-user", PlexusUserResourceResponse.class );
        xstream.omitField( PlexusUserResource.class, "modelEncoding" );
        xstream.registerLocalConverter( PlexusUserResource.class, "roles", new AliasingListConverter(
            PlexusRoleResource.class,
            "plexus-role" ) );

        xstream.omitField( PlexusRoleResource.class, "modelEncoding" );
        xstream.alias( "plexus-role", PlexusRoleResource.class );

        xstream.omitField( PlexusUserListResourceResponse.class, "modelEncoding" );
        xstream.alias( "plexus-user-list", PlexusUserListResourceResponse.class );
        xstream.registerLocalConverter( PlexusUserListResourceResponse.class, "data", new AliasingListConverter(
            PlexusUserResource.class,
            "plexus-user" ) );

        xstream.omitField( ExternalRoleMappingResourceResponse.class, "modelEncoding" );
        xstream.alias( "external-role-mapping", ExternalRoleMappingResourceResponse.class );
        xstream.registerLocalConverter( ExternalRoleMappingResourceResponse.class, "data", new AliasingListConverter(
            ExternalRoleMappingResource.class,
            "mapping" ) );
        xstream.omitField( ExternalRoleMappingResource.class, "modelEncoding" );
        xstream.alias( "mapping", ExternalRoleMappingResource.class );
        xstream.registerLocalConverter( ExternalRoleMappingResource.class, "mappedRoles", new AliasingListConverter(
            PlexusRoleResource.class,
            "plexus-role" ) );

        xstream.omitField( PlexusRoleListResourceResponse.class, "modelEncoding" );
        xstream.alias( "plexus-roles", PlexusRoleListResourceResponse.class );
        xstream.registerLocalConverter( PlexusRoleListResourceResponse.class, "data", new AliasingListConverter(
            PlexusRoleResource.class,
            "plexus-role" ) );

        
        xstream.omitField( PlexusUserSearchCriteriaResourceRequest.class, "modelEncoding" );
        xstream.alias( "user-search", PlexusUserSearchCriteriaResourceRequest.class );
        xstream.omitField( PlexusUserSearchCriteriaResource.class, "modelEncoding" );
        
        xstream.omitField( MirrorResourceListRequest.class, "modelEncoding" );
        xstream.omitField( MirrorResourceListResponse.class, "modelEncoding" );
        xstream.omitField( MirrorStatusResourceListResponse.class, "modelEncoding" );
        xstream.omitField( MirrorResource.class, "modelEncoding" );
        xstream.omitField( MirrorStatusResource.class, "modelEncoding" );
        
        xstream.alias( "mirror-list-request", MirrorResourceListRequest.class );
        xstream.alias( "mirror-list-response", MirrorResourceListResponse.class );
        xstream.alias( "mirror-status-list-response", MirrorStatusResourceListResponse.class );
        
        xstream.registerLocalConverter( MirrorResourceListRequest.class, "data", new AliasingListConverter(
            MirrorResource.class,
            "mirrorResource" ) );
        xstream.registerLocalConverter( MirrorResourceListResponse.class, "data", new AliasingListConverter(
            MirrorResource.class,
            "mirrorResource" ) );
        xstream.registerLocalConverter( MirrorStatusResourceListResponse.class, "data", new AliasingListConverter(
            MirrorStatusResource.class,
            "mirrorResource" ) );
        
        // Maven model
        xstream.omitField( Model.class, "modelEncoding" );
        xstream.omitField( ModelBase.class, "modelEncoding" );
        xstream.omitField( Scm.class, "modelEncoding" );

        return xstream;
    }

    @Override
    protected Router initializeRouter( Router root, boolean isStarted )
    {
        // ========
        // SERVICE

        // service router
        Router applicationRouter = new Router( getContext() );

        // attaching filter to a root on given URI
        attach( root, false, "/service/" + AbstractNexusPlexusResource.NEXUS_INSTANCE_LOCAL, applicationRouter );

        // return the swapped router
        return applicationRouter;
    }

    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        // SERVICE (two always connected, unrelated to isStarted)

        attach( getApplicationRouter(), false, statusPlexusResource );

        attach( getApplicationRouter(), false, commandPlexusResource );

        if ( !isStarted )
        {
            return;
        }

        // ==========
        // INDEX.HTML and WAR contents
        // To redirect "uncaught" requests to indexTemplateResource
        attach( root, true, "", new PlexusResourceFinder( getContext(), indexRedirectingResource ) );
        attach( root, true, "/", new PlexusResourceFinder( getContext(), indexRedirectingResource ) );

        // the indexTemplateResource
        attach( root, true, indexTemplateResource );

        // publish the WAR contents
        Directory rootDir = new Directory( getContext(), "war:///" );
        rootDir.setListingAllowed( false );
        rootDir.setNegotiateContent( false );
        attach( root, false, "/", rootDir );

        // ================
        // STATIC RESOURCES

        if ( nexusResourceBundles.size() > 0 )
        {
            for ( NexusResourceBundle bundle : nexusResourceBundles )
            {
                List<StaticResource> resources = bundle.getContributedResouces();

                if ( resources != null )
                {
                    for ( StaticResource resource : resources )
                    {
                        attach( root, true, resource.getPath(), new StaticResourceFinder( getContext(), resource ) );
                    }
                }
            }
        }

        // =======
        // CONTENT

        // prepare for browser diversity :)
        BrowserSensingFilter bsf = new BrowserSensingFilter( getContext() );

        // mounting it
        attach( root, false, "/content", bsf );

        bsf.setNext( new PlexusResourceFinder( getContext(), contentResource ) );

        // protecting the content service manually
        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource( "/content"
                    + contentResource.getResourceProtection().getPathPattern(), contentResource
                    .getResourceProtection().getFilterExpression() );
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to protect resource mounted to "
                    + contentResource.getResourceUri() + " of class " + contentResource.getClass().getName(), e );
            }
        }

        // protecting service resources with "wall" permission
        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                // TODO: recheck this? We are adding a flat wall to be hit if a mapping is missed
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource(
                    "/service/**",
                    "authcBasic,perms[nexus:permToCatchAllUnprotecteds]" );
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to add WALL to the end of the chain", e );
            }

            // signal we finished adding resources
            ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).protectedResourcesAdded();
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
            }
            catch ( SecurityConfigurationException e )
            {
                throw new IllegalStateException( "Could not configure JSecurity to protect resource mounted to "
                    + resource.getResourceUri() + " of class " + resource.getClass().getName(), e );
            }
        }
    }
}

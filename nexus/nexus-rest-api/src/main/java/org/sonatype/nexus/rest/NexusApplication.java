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
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.rest.model.ArtifactResolveResourceResponse;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;
import org.sonatype.nexus.rest.model.ContentListDescribeRequestResource;
import org.sonatype.nexus.rest.model.ContentListDescribeResourceResponse;
import org.sonatype.nexus.rest.model.ContentListDescribeResponseResource;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.ErrorReportRequest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.FeedListResource;
import org.sonatype.nexus.rest.model.FeedListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.LogConfigResourceResponse;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListRequest;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResource;
import org.sonatype.nexus.rest.model.NexusRepositoryTypeListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusComponentListResource;
import org.sonatype.nexus.rest.model.PlexusComponentListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryDependentStatusResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusListResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.model.SmtpSettingsResourceRequest;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.nexus.rest.repositories.RepositoryBaseResourceConverter;
import org.sonatype.nexus.rest.repositories.RepositoryResourceResponseConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServicePropertyResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceResponseConverter;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.RetargetableRestlet;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.security.rest.model.ClientPermission;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.ExternalRoleMappingResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleListResourceResponse;
import org.sonatype.security.rest.model.PlexusRoleResource;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserResourceResponse;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.security.rest.model.PrivilegeListResourceResponse;
import org.sonatype.security.rest.model.PrivilegeProperty;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.sonatype.security.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.security.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.security.rest.model.PrivilegeTypeResource;
import org.sonatype.security.rest.model.PrivilegeTypeResourceResponse;
import org.sonatype.security.rest.model.RoleListResourceResponse;
import org.sonatype.security.rest.model.RoleResource;
import org.sonatype.security.rest.model.RoleResourceRequest;
import org.sonatype.security.rest.model.RoleResourceResponse;
import org.sonatype.security.rest.model.UserChangePasswordRequest;
import org.sonatype.security.rest.model.UserForgotPasswordRequest;
import org.sonatype.security.rest.model.UserListResourceResponse;
import org.sonatype.security.rest.model.UserResource;
import org.sonatype.security.rest.model.UserResourceRequest;
import org.sonatype.security.rest.model.UserResourceResponse;
import org.sonatype.security.rest.model.UserToRoleResource;
import org.sonatype.security.rest.model.UserToRoleResourceRequest;
import org.sonatype.security.web.PlexusMutableWebConfiguration;
import org.sonatype.security.web.PlexusWebConfiguration;
import org.sonatype.security.web.SecurityConfigurationException;

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
    private ApplicationEventMulticaster applicationEventMulticaster;

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

    @Requirement( role = NexusApplicationCustomizer.class )
    private List<NexusApplicationCustomizer> customizers;

    @Requirement( role = ErrorReportingManager.class )
    private ErrorReportingManager errorManager;

    /**
     * Listener.
     */
    public void onEvent( Event<?> evt )
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
        // NEXUS-2883: turning off Range support for now
        getRangeService().setEnabled( false );

        // adding ourselves as listener
        applicationEventMulticaster.addEventListener( this );
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
        
        xstream.processAnnotations( ErrorReportResponse.class );
        xstream.processAnnotations( ErrorReportRequest.class );
        xstream.processAnnotations( ArtifactResolveResourceResponse.class );
        xstream.processAnnotations( GlobalConfigurationListResourceResponse.class );
        xstream.processAnnotations( GlobalConfigurationResourceResponse.class );
        xstream.processAnnotations( RepositoryStatusListResourceResponse.class );
        xstream.processAnnotations( RepositoryListResourceResponse.class );
        xstream.processAnnotations( RepositoryResourceResponse.class );
        xstream.processAnnotations( RepositoryStatusResourceResponse.class );
        xstream.processAnnotations( RepositoryMetaResourceResponse.class );
        xstream.processAnnotations( RepositoryGroupListResourceResponse.class );
        xstream.processAnnotations( RepositoryGroupResourceResponse.class );
        xstream.processAnnotations( RepositoryRouteListResourceResponse.class );
        xstream.processAnnotations( RepositoryRouteResourceResponse.class );
        xstream.processAnnotations( ScheduledServiceListResourceResponse.class );
        xstream.processAnnotations( ScheduledServiceResourceStatusResponse.class );
        xstream.processAnnotations( ScheduledServiceResourceResponse.class );
        xstream.processAnnotations( ScheduledServiceTypeResourceResponse.class );
        xstream.processAnnotations( ContentListResourceResponse.class );
        xstream.processAnnotations( ContentListDescribeResourceResponse.class );
        xstream.processAnnotations( LogsListResourceResponse.class );
        xstream.processAnnotations( ConfigurationsListResourceResponse.class );
        xstream.processAnnotations( FeedListResourceResponse.class );
        xstream.processAnnotations( NFCResourceResponse.class );
        xstream.processAnnotations( StatusResourceResponse.class );
        xstream.processAnnotations( WastebasketResourceResponse.class );
        xstream.processAnnotations( RepositoryTargetListResourceResponse.class );
        xstream.processAnnotations( RepositoryTargetResourceResponse.class );
        xstream.processAnnotations( RepositoryContentClassListResourceResponse.class );
        xstream.processAnnotations( LogConfigResourceResponse.class );
        xstream.processAnnotations( MirrorResourceListResponse.class );
        xstream.processAnnotations( MirrorResourceListRequest.class );
        xstream.processAnnotations( MirrorStatusResourceListResponse.class );
        xstream.processAnnotations( SmtpSettingsResourceRequest.class );
        xstream.processAnnotations( PlexusComponentListResourceResponse.class );
        xstream.processAnnotations( NexusRepositoryTypeListResourceResponse.class );
        xstream.processAnnotations( PrivilegeResourceRequest.class );

        xstream.alias( "nexus-error", ErrorResponse.class );
        xstream.alias( "error", ErrorMessage.class );
        xstream.registerLocalConverter( ErrorResponse.class, "errors", new AliasingListConverter( ErrorMessage.class,
            "error" ) );

        xstream.registerLocalConverter( ContentListResourceResponse.class, "data", new AliasingListConverter(
            ContentListResource.class, "content-item" ) );

        xstream.registerLocalConverter( RepositoryListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryListResource.class, "repositories-item" ) );

        xstream.registerLocalConverter( NexusRepositoryTypeListResourceResponse.class, "data",
            new AliasingListConverter( NexusRepositoryTypeListResource.class, "repositoryType" ) );

        xstream.registerLocalConverter( RepositoryStatusListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryStatusListResource.class, "repository-status-list-item" ) );

        xstream.registerLocalConverter( RepositoryGroupListResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class, "repo-group-member" ) );
        xstream.registerLocalConverter( RepositoryGroupResource.class, "repositories", new AliasingListConverter(
            RepositoryGroupMemberRepository.class, "repo-group-member" ) );
        xstream.registerLocalConverter( RepositoryGroupListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryGroupListResource.class, "repo-group-list-item" ) );

        xstream.registerLocalConverter( RepositoryRouteListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryRouteListResource.class, "repo-routes-list-item" ) );
        xstream.registerLocalConverter( RepositoryRouteListResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class, "repo-routes-member" ) );

        xstream.registerLocalConverter( RepositoryRouteResource.class, "repositories", new AliasingListConverter(
            RepositoryRouteMemberRepository.class, "repository" ) );

        xstream.registerLocalConverter( GlobalConfigurationListResourceResponse.class, "data",
            new AliasingListConverter( GlobalConfigurationListResource.class, "global-settings-list-item" ) );

        xstream.registerLocalConverter( LogsListResourceResponse.class, "data", new AliasingListConverter(
            LogsListResource.class, "logs-list-item" ) );

        xstream.registerLocalConverter( ConfigurationsListResourceResponse.class, "data", new AliasingListConverter(
            ConfigurationsListResource.class, "configs-list-item" ) );

        xstream.registerLocalConverter( FeedListResourceResponse.class, "data", new AliasingListConverter(
            FeedListResource.class, "feeds-list-item" ) );

        xstream.alias( "authentication-login", AuthenticationLoginResourceResponse.class ); // Look at
        // NexusAuthenticationLoginResourceConverter,
        // we are only converting
        // the clientPermissions
        // field

        xstream.registerLocalConverter( AuthenticationClientPermissions.class, "permissions",
            new AliasingListConverter( ClientPermission.class, "permission" ) );

        xstream.registerLocalConverter( StatusConfigurationValidationResponse.class, "validationErrors",
            new AliasingListConverter( String.class, "error" ) );
        xstream.registerLocalConverter( StatusConfigurationValidationResponse.class, "validationWarnings",
            new AliasingListConverter( String.class, "warning" ) );

        xstream.registerLocalConverter( ScheduledServiceBaseResource.class, "properties", new AliasingListConverter(
            ScheduledServicePropertyResource.class, "scheduled-task-property" ) );
        xstream.registerLocalConverter( ScheduledServiceWeeklyResource.class, "recurringDay",
            new AliasingListConverter( String.class, "day" ) );
        xstream.registerLocalConverter( ScheduledServiceTypeResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceTypeResource.class, "schedule-type" ) );
        xstream.registerLocalConverter( ScheduledServiceTypeResource.class, "properties", new AliasingListConverter(
            ScheduledServiceTypePropertyResource.class, "scheduled-task-property" ) );
        xstream.registerLocalConverter( ScheduledServiceListResourceResponse.class, "data", new AliasingListConverter(
            ScheduledServiceListResource.class, "schedules-list-item" ) );

        xstream.aliasField( "methods", PrivilegeResource.class, "method" );

        xstream.registerLocalConverter( NFCResource.class, "nfcContents", new AliasingListConverter(
            NFCRepositoryResource.class, "nfc-repo-info" ) );
        xstream.registerLocalConverter( NFCRepositoryResource.class, "nfcPaths", new AliasingListConverter(
            String.class, "path" ) );

        xstream.registerLocalConverter( RepositoryTargetResource.class, "patterns", new AliasingListConverter(
            String.class, "pattern" ) );
        xstream.registerLocalConverter( RepositoryTargetListResourceResponse.class, "data", new AliasingListConverter(
            RepositoryTargetListResource.class, "repo-targets-list-item" ) );

        xstream.registerLocalConverter( RepositoryContentClassListResourceResponse.class, "data",
            new AliasingListConverter( RepositoryContentClassListResource.class, "repo-content-classes-list-item" ) );

        xstream.registerLocalConverter( PlexusComponentListResourceResponse.class, "data", new AliasingListConverter(
            PlexusComponentListResource.class, "component" ) );

        xstream.registerLocalConverter( MirrorResourceListRequest.class, "data", new AliasingListConverter(
            MirrorResource.class, "mirrorResource" ) );
        xstream.registerLocalConverter( MirrorResourceListResponse.class, "data", new AliasingListConverter(
            MirrorResource.class, "mirrorResource" ) );
        xstream.registerLocalConverter( MirrorStatusResourceListResponse.class, "data", new AliasingListConverter(
            MirrorStatusResource.class, "mirrorResource" ) );
        
        xstream.registerLocalConverter( ContentListDescribeRequestResource.class, "requestContext", new AliasingListConverter(
            String.class, "requestContextItem" ) );
        
        xstream.registerLocalConverter( ContentListDescribeResponseResource.class, "appliedMappings", new AliasingListConverter(
            String.class, "appliedMappingItem" ) );        
        xstream.registerLocalConverter( ContentListDescribeResponseResource.class, "attributes", new AliasingListConverter(
            String.class, "attributeItem" ) );        
        xstream.registerLocalConverter( ContentListDescribeResponseResource.class, "processedRepositoriesList", new AliasingListConverter(
            String.class, "processedRepositoriesListItem" ) );        
        xstream.registerLocalConverter( ContentListDescribeResponseResource.class, "properties", new AliasingListConverter(
            String.class, "propertyItem" ) );        
        xstream.registerLocalConverter( ContentListDescribeResponseResource.class, "sources", new AliasingListConverter(
            String.class, "sourceItem" ) );
        
        xstream.registerLocalConverter( RepositoryStatusResource.class, "dependentRepos", new AliasingListConverter(
            RepositoryDependentStatusResource.class, "dependentRepoItem" ) );
        
        xstream.registerLocalConverter( GlobalConfigurationResource.class, "securityRealms", new AliasingListConverter(
            String.class, "securityRealmItem" ) );

        // Maven model
        xstream.omitField( Model.class, "modelEncoding" );
        xstream.omitField( ModelBase.class, "modelEncoding" );
        xstream.omitField( Scm.class, "modelEncoding" );
        
        // SECURITY below
        xstream.processAnnotations( AuthenticationLoginResourceResponse.class );
        xstream.processAnnotations( UserResourceResponse.class );
        xstream.processAnnotations( UserListResourceResponse.class );
        xstream.processAnnotations( UserResourceRequest.class );
        xstream.processAnnotations( UserForgotPasswordRequest.class );
        xstream.processAnnotations( UserChangePasswordRequest.class );
        xstream.registerLocalConverter( UserResource.class, "roles", new AliasingListConverter( String.class, "role" ) );
        xstream.registerLocalConverter( UserListResourceResponse.class, "data",
                                        new AliasingListConverter( UserResource.class, "users-list-item" ) );

        xstream.processAnnotations( RoleListResourceResponse.class );
        xstream.processAnnotations( RoleResource.class );
        xstream.processAnnotations( RoleResourceRequest.class );

        xstream.processAnnotations( RoleResourceResponse.class );
        xstream.registerLocalConverter( RoleListResourceResponse.class, "data",
                                        new AliasingListConverter( RoleResource.class, "roles-list-item" ) );
        xstream.registerLocalConverter( RoleResource.class, "roles", new AliasingListConverter( String.class, "role" ) );
        xstream.registerLocalConverter( RoleResource.class, "privileges", new AliasingListConverter( String.class,
                                                                                                     "privilege" ) );

        xstream.processAnnotations( PrivilegeListResourceResponse.class );
        xstream.processAnnotations( PrivilegeStatusResourceResponse.class );
        xstream.processAnnotations( PrivilegeTypeResourceResponse.class );
        xstream.registerLocalConverter( PrivilegeListResourceResponse.class, "data",
                                        new AliasingListConverter( PrivilegeStatusResource.class, "privilege-item" ) );
        xstream.registerLocalConverter( PrivilegeResource.class, "method", new AliasingListConverter( String.class,
                                                                                                      "method" ) );
        xstream.registerLocalConverter( PrivilegeStatusResource.class, "properties",
                                        new AliasingListConverter( PrivilegeProperty.class, "privilege-property" ) );
        xstream.registerLocalConverter( PrivilegeTypeResourceResponse.class, "data",
                                        new AliasingListConverter( PrivilegeTypeResource.class, "privilege-type" ) );
        xstream.registerLocalConverter( PrivilegeTypeResource.class, "properties",
                                        new AliasingListConverter( PrivilegeTypePropertyResource.class,
                                                                   "privilege-type-property" ) );

        xstream.processAnnotations( UserToRoleResourceRequest.class );
        xstream.registerLocalConverter( UserToRoleResource.class, "roles", new AliasingListConverter( String.class,
                                                                                                      "role" ) );

        xstream.processAnnotations( PlexusUserResourceResponse.class );
        xstream.registerLocalConverter( PlexusUserResource.class, "roles",
                                        new AliasingListConverter( PlexusRoleResource.class, "plexus-role" ) );

        xstream.processAnnotations( PlexusRoleResource.class );

        xstream.processAnnotations( PlexusUserListResourceResponse.class );
        xstream.registerLocalConverter( PlexusUserListResourceResponse.class, "data",
                                        new AliasingListConverter( PlexusUserResource.class, "plexus-user" ) );

        xstream.processAnnotations( ExternalRoleMappingResourceResponse.class );
        xstream.processAnnotations( ExternalRoleMappingResource.class );

        xstream.registerLocalConverter( ExternalRoleMappingResourceResponse.class, "data",
                                        new AliasingListConverter( ExternalRoleMappingResource.class, "mapping" ) );
        xstream.registerLocalConverter( ExternalRoleMappingResource.class, "mappedRoles",
                                        new AliasingListConverter( PlexusRoleResource.class, "plexus-role" ) );

        xstream.processAnnotations( PlexusRoleListResourceResponse.class );
        xstream.registerLocalConverter( PlexusRoleListResourceResponse.class, "data",
                                        new AliasingListConverter( PlexusRoleResource.class, "plexus-role" ) );

        xstream.processAnnotations( PlexusUserSearchCriteriaResourceRequest.class );

        xstream.processAnnotations( org.sonatype.security.rest.model.PlexusComponentListResourceResponse.class );
        xstream.processAnnotations( org.sonatype.security.rest.model.PlexusComponentListResource.class );
        xstream.registerLocalConverter(
                                        org.sonatype.security.rest.model.PlexusComponentListResourceResponse.class,
                                        "data",
                                        new AliasingListConverter(
                                        org.sonatype.security.rest.model.PlexusComponentListResource.class, "component" ) );

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

    @Override
    protected void afterCreateRoot( RetargetableRestlet root )
    {
        // customizers
        for ( NexusApplicationCustomizer customizer : customizers )
        {
            customizer.customize( this, root );
        }
    }

    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        if ( !isStarted )
        {
            return;
        }
        
        // Add error manager to context
        getContext().getAttributes().put( ErrorReportingManager.class.getName(), errorManager );

        // SERVICE (two always connected, unrelated to isStarted)

        attach( getApplicationRouter(), false, statusPlexusResource );

        attach( getApplicationRouter(), false, commandPlexusResource );

        // ==========
        // INDEX.HTML and WAR contents
        // To redirect "uncaught" requests to indexTemplateResource
        attach( root, true, "", new NexusPlexusResourceFinder( getContext(), indexRedirectingResource ) );
        attach( root, true, "/", new NexusPlexusResourceFinder( getContext(), indexRedirectingResource ) );

        // the indexTemplateResource
        attach( root, true, indexTemplateResource );

        // publish the WAR contents
        Directory rootDir = new NexusDirectory( getContext(), "war:///" );
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
                        attach( root, false, resource.getPath(), new StaticResourceFinder( getContext(), resource ) );
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

        bsf.setNext( new NexusPlexusResourceFinder( getContext(), contentResource ) );

        // protecting the content service manually
        if ( PlexusMutableWebConfiguration.class.isAssignableFrom( plexusWebConfiguration.getClass() ) )
        {
            try
            {
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource( "/content"
                    + contentResource.getResourceProtection().getPathPattern(), contentResource.getResourceProtection()
                    .getFilterExpression() );
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
                ( (PlexusMutableWebConfiguration) plexusWebConfiguration ).addProtectedResource( "/service/**",
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

    @Override
    protected void attach( Router router, boolean strict, PlexusResource resource )
    {
        attach( router, strict, resource.getResourceUri(), new NexusPlexusResourceFinder( getContext(), resource ) );

        handlePlexusResourceSecurity( resource );
    }
}

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
package org.sonatype.nexus.rest.xstream;

import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Scm;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.nexus.rest.model.FeedListResource;
import org.sonatype.nexus.rest.model.FeedListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResourceResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationResourceResponse;
import org.sonatype.nexus.rest.model.LogsListResource;
import org.sonatype.nexus.rest.model.LogsListResourceResponse;
import org.sonatype.nexus.rest.model.NFCRepositoryResource;
import org.sonatype.nexus.rest.model.NFCResource;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.nexus.rest.model.NexusResponse;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeListResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeResourceRequest;
import org.sonatype.nexus.rest.model.PrivilegeStatusResourceResponse;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResource;
import org.sonatype.nexus.rest.model.RepositoryContentClassListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryGroupResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryMetaResource;
import org.sonatype.nexus.rest.model.RepositoryMetaResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResource;
import org.sonatype.nexus.rest.model.RepositoryStatusListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetListResource;
import org.sonatype.nexus.rest.model.RepositoryTargetListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResourceResponse;
import org.sonatype.nexus.rest.model.RoleListResourceResponse;
import org.sonatype.nexus.rest.model.RoleResource;
import org.sonatype.nexus.rest.model.RoleResourceRequest;
import org.sonatype.nexus.rest.model.RoleResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.nexus.rest.model.StatusConfigurationValidationResponse;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.rest.model.UserChangePasswordRequest;
import org.sonatype.nexus.rest.model.UserChangePasswordResource;
import org.sonatype.nexus.rest.model.UserForgotPasswordRequest;
import org.sonatype.nexus.rest.model.UserForgotPasswordResource;
import org.sonatype.nexus.rest.model.UserListResourceResponse;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.model.UserResourceRequest;
import org.sonatype.nexus.rest.model.UserResourceResponse;
import org.sonatype.nexus.rest.model.WastebasketResource;
import org.sonatype.nexus.rest.model.WastebasketResourceResponse;
import org.sonatype.nexus.rest.privileges.PrivilegeBaseResourceConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeBaseStatusResourceConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeResourceRequestConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeStatusResourceResponseConverter;
import org.sonatype.nexus.rest.repositories.RepositoryBaseResourceConverter;
import org.sonatype.nexus.rest.repositories.RepositoryResourceResponseConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServicePropertyResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceResponseConverter;

import com.thoughtworks.xstream.XStream;

public final class XStreamInitializer
{
    public static XStream initialize( XStream xstream )
    {
        xstream.registerConverter( new RepositoryBaseResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new RepositoryResourceResponseConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from data
        
        xstream.registerConverter( new ScheduledServiceBaseResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new ScheduledServicePropertyResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new ScheduledServiceResourceResponseConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from data
        
        xstream.registerConverter( new PrivilegeBaseResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new PrivilegeResourceRequestConverter( xstream.getMapper(), xstream
             .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from data
        xstream.registerConverter( new PrivilegeBaseStatusResourceConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );
        xstream.registerConverter( new PrivilegeStatusResourceResponseConverter( xstream.getMapper(), xstream
            .getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH ); // strips the class="class.name" attribute from data

        // Maven POM
        xstream.alias( "project", Model.class );
        
        
        // omitting modelEncoding
        xstream.omitField( NexusErrorResponse.class, "modelEncoding" );
//        xstream.addImplicitCollection( NexusErrorResponse.class, "errors", "error", NexusError.class ); // FIXME: this might break the JSON parser, test it before checking in
        xstream.omitField( NexusError.class, "modelEncoding" );
        xstream.alias( "nexus-error", NexusErrorResponse.class);
        xstream.alias( "error", NexusError.class);

        xstream.omitField( ContentListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ContentListResource.class, "modelEncoding" );
        xstream.alias( "content", ContentListResourceResponse.class);
        xstream.alias( "content-item", ContentListResource.class);
        

        xstream.omitField( RepositoryResourceResponse.class, "modelEncoding" );        
        xstream.omitField( RepositoryBaseResource.class, "modelEncoding" );
        xstream.omitField( RepositoryResource.class, "modelEncoding" );
        xstream.omitField( RepositoryProxyResource.class, "modelEncoding" );
        xstream.omitField( RepositoryShadowResource.class, "modelEncoding" );
        xstream.omitField( RepositoryResourceRemoteStorage.class, "modelEncoding" );
        xstream.alias( "repository", RepositoryResourceResponse.class);
        

        xstream.omitField( RepositoryListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryListResource.class, "modelEncoding" );
        xstream.alias( "repositories", RepositoryListResourceResponse.class);
        xstream.alias( "repositories-item", RepositoryListResource.class);

        xstream.omitField( RepositoryStatusResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusResource.class, "modelEncoding" );
        xstream.alias( "repository-status", RepositoryStatusResourceResponse.class);

        xstream.omitField( RepositoryStatusListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusListResource.class, "modelEncoding" );
        xstream.alias( "repository-status-list", RepositoryStatusListResourceResponse.class);
        xstream.alias( "repository-status-list-item", RepositoryStatusListResource.class);

        xstream.omitField( RepositoryMetaResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryMetaResource.class, "modelEncoding" );
        xstream.alias( "repository-meta-data", RepositoryMetaResourceResponse.class);

        xstream.omitField( RepositoryGroupListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupListResource.class, "modelEncoding" );
        xstream.alias( "repo-group-list", RepositoryGroupListResourceResponse.class);
        xstream.alias( "repo-group-list-item", RepositoryGroupListResource.class);
        xstream.alias( "repo-group-memeber", RepositoryGroupMemberRepository.class);

        xstream.omitField( RepositoryGroupResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupResource.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-group", RepositoryGroupResourceResponse.class);

        xstream.omitField( RepositoryRouteListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteListResource.class, "modelEncoding" );
        xstream.alias( "repo-routes-list", RepositoryRouteListResourceResponse.class);
        xstream.alias( "repo-routes-list-item", RepositoryRouteListResource.class);
        xstream.alias( "repo-routes-member", RepositoryRouteMemberRepository.class);

        xstream.omitField( RepositoryRouteResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteResource.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-route", RepositoryRouteResourceResponse.class);

        xstream.omitField( GlobalConfigurationListResourceResponse.class, "modelEncoding" );
        xstream.omitField( GlobalConfigurationListResource.class, "modelEncoding" );
        xstream.alias( "global-settings-list", GlobalConfigurationListResourceResponse.class);
        xstream.alias( "global-settings-list-item", GlobalConfigurationListResource.class);
        

        xstream.omitField( GlobalConfigurationResourceResponse.class, "modelEncoding" );
        xstream.omitField( GlobalConfigurationResource.class, "modelEncoding" );
        xstream.omitField( RemoteConnectionSettings.class, "modelEncoding" );
        xstream.omitField( RemoteHttpProxySettings.class, "modelEncoding" );
        xstream.omitField( AuthenticationSettings.class, "modelEncoding" );
        xstream.omitField( SmtpSettings.class, "modelEncoding" );
        xstream.alias( "global-settings", GlobalConfigurationResourceResponse.class);

        xstream.omitField( WastebasketResource.class, "modelEncoding" );
        xstream.omitField( WastebasketResourceResponse.class, "modelEncoding" );
        xstream.alias( "wastebasket", WastebasketResourceResponse.class);
        
        xstream.omitField( LogsListResourceResponse.class, "modelEncoding" );
        xstream.omitField( LogsListResource.class, "modelEncoding" );
        xstream.alias( "logs-list", LogsListResourceResponse.class);
        xstream.alias( "logs-list-item", LogsListResource.class);

        xstream.omitField( ConfigurationsListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ConfigurationsListResource.class, "modelEncoding" );
        xstream.alias( "configs-list", ConfigurationsListResourceResponse.class);
        xstream.alias( "configs-list-tem", ConfigurationsListResource.class);

        xstream.omitField( FeedListResourceResponse.class, "modelEncoding" );
        xstream.omitField( FeedListResource.class, "modelEncoding" );
        xstream.alias( "feeds-list", FeedListResourceResponse.class);
        xstream.alias( "feeds-list-item", FeedListResource.class);

        xstream.omitField( SearchResponse.class, "modelEncoding" );
        xstream.alias( "search-results", SearchResponse.class);

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
        xstream.alias( "schedules-list-item", ScheduledServiceListResource.class );
        xstream.alias( "scheduled-task", ScheduledServiceResourceResponse.class );
        xstream.alias( "scheduled-task-property", ScheduledServicePropertyResource.class );
        xstream.alias( "schedule-types", ScheduledServiceTypeResourceResponse.class );
        xstream.alias( "schedule-type", ScheduledServiceTypeResource.class );
        xstream.alias( "schedule-type-property", ScheduledServiceTypePropertyResource.class );
        
        xstream.omitField( UserListResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResourceRequest.class, "modelEncoding" );
        xstream.omitField( UserResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResource.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordResource.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordResource.class, "modelEncoding" );
        xstream.alias( "users-list", UserListResourceResponse.class );
        xstream.alias( "users-list-item", UserResource.class );
        xstream.alias( "user-request", UserResourceRequest.class );
        xstream.alias( "user-response", UserResourceResponse.class );
        xstream.alias( "user-forgotpw", UserForgotPasswordRequest.class );
        xstream.alias( "user-changepw", UserChangePasswordRequest.class );

        xstream.omitField( RoleListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RoleResource.class, "modelEncoding" );
        xstream.omitField( RoleResourceRequest.class, "modelEncoding" );
        xstream.omitField( RoleResourceResponse.class, "modelEncoding" );
        xstream.alias( "roles-list", RoleListResourceResponse.class );
        xstream.alias( "roles-list-item", RoleResource.class );
        xstream.alias( "role-request", RoleResourceRequest.class );
        xstream.alias( "role-response", RoleResourceResponse.class );
        
        xstream.omitField( PrivilegeResourceRequest.class, "modelEncoding" );
        xstream.omitField( PrivilegeTargetResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeBaseStatusResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeApplicationStatusResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeTargetStatusResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeListResourceResponse.class, "modelEncoding" );
        xstream.omitField( PrivilegeBaseResource.class, "modelEncoding" );
        xstream.omitField( PrivilegeStatusResourceResponse.class, "modelEncoding" );
        xstream.alias( "privilege", PrivilegeResourceRequest.class );
        xstream.alias( "privileges-status-list", PrivilegeListResourceResponse.class );
        xstream.alias( "privilege-target-status", PrivilegeTargetStatusResource.class );
        xstream.alias( "privilege-application-status", PrivilegeApplicationStatusResource.class );
        xstream.alias( "privilege-status", PrivilegeStatusResourceResponse.class );
        

        xstream.omitField( NFCResourceResponse.class, "modelEncoding" );
        xstream.omitField( NFCResource.class, "modelEncoding" );
        xstream.omitField( NFCRepositoryResource.class, "modelEncoding" );
        xstream.alias( "nfc-info", NFCResourceResponse.class );
        xstream.alias( "nfc-repo-info", NFCRepositoryResource.class );

        xstream.omitField( RepositoryTargetListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetListResource.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResource.class, "modelEncoding" );
        xstream.alias( "repo-targets-list", RepositoryTargetListResourceResponse.class );
        xstream.alias( "repo-targets-list-item", RepositoryTargetListResource.class );
        xstream.alias( "repo-target", RepositoryTargetResourceResponse.class );

        xstream.omitField( RepositoryContentClassListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryContentClassListResource.class, "modelEncoding" );
        xstream.alias( "repo-content-classes-list", RepositoryContentClassListResourceResponse.class );
        xstream.alias( "repo-content-classes-list-item", RepositoryContentClassListResource.class );

        // Maven model
        xstream.omitField( Model.class, "modelEncoding" );
        xstream.omitField( ModelBase.class, "modelEncoding" );
        xstream.omitField( Scm.class, "modelEncoding" );
        return xstream;
    }
}

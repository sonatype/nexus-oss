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
import org.sonatype.nexus.rest.model.*;
import org.sonatype.nexus.rest.privileges.PrivilegeBaseResourceConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeBaseStatusResourceConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeResourceRequestConverter;
import org.sonatype.nexus.rest.privileges.PrivilegeStatusResourceResponseConverter;
import org.sonatype.nexus.rest.repositories.RepositoryBaseResourceConverter;
import org.sonatype.nexus.rest.repositories.RepositoryResourceResponseConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServicePropertyResourceConverter;
import org.sonatype.nexus.rest.schedules.ScheduledServiceResourceResponseConverter;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

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
        
        
//        xstream.registerLocalConverter( PrivilegeListResourceResponse.class, "data", new AliasingListConverter( PrivilegeApplicationStatusResource.class, "privilege-application-status"));
        
        // Maven POM
        xstream.alias( "project", Model.class );
        
        
        // omitting modelEncoding
        xstream.omitField( NexusErrorResponse.class, "modelEncoding" );
//        xstream.addImplicitCollection( NexusErrorResponse.class, "errors", "error", NexusError.class ); // FIXME: this might break the JSON parser, test it before checking in
        xstream.omitField( NexusError.class, "modelEncoding" );
        xstream.alias( "nexus-error", NexusErrorResponse.class);
        xstream.alias( "error", NexusError.class);
        xstream.registerLocalConverter( NexusErrorResponse.class, "errors", new AliasingListConverter( NexusError.class, "error"));
        
        xstream.omitField( ContentListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ContentListResource.class, "modelEncoding" );
        xstream.alias( "content", ContentListResourceResponse.class);
        xstream.alias( "content-item", ContentListResource.class);
        xstream.registerLocalConverter( ContentListResourceResponse.class, "data", new AliasingListConverter( ContentListResource.class, "content-item"));
        

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
//        xstream.alias( "repositories-item", RepositoryListResource.class);
        xstream.registerLocalConverter( RepositoryListResourceResponse.class, "data", new AliasingListConverter( RepositoryListResource.class, "repositories-item"));
        

        xstream.omitField( RepositoryStatusResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusResource.class, "modelEncoding" );
        xstream.alias( "repository-status", RepositoryStatusResourceResponse.class);

        xstream.omitField( RepositoryStatusListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryStatusListResource.class, "modelEncoding" );
        xstream.alias( "repository-status-list", RepositoryStatusListResourceResponse.class);
//        xstream.alias( "repository-status-list-item", RepositoryStatusListResource.class);
        xstream.registerLocalConverter( RepositoryStatusListResourceResponse.class, "data", new AliasingListConverter( RepositoryStatusListResource.class, "repository-status-list-item"));
        
        xstream.omitField( RepositoryMetaResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryMetaResource.class, "modelEncoding" );
        xstream.alias( "repository-meta-data", RepositoryMetaResourceResponse.class);

        xstream.omitField( RepositoryGroupListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupListResource.class, "modelEncoding" );
        xstream.alias( "repo-group-list", RepositoryGroupListResourceResponse.class);
//        xstream.alias( "repo-group-list-item", RepositoryGroupListResource.class);
//        xstream.alias( "repo-group-memeber", RepositoryGroupMemberRepository.class);
        xstream.registerLocalConverter( RepositoryGroupListResource.class, "repositories", new AliasingListConverter( RepositoryGroupMemberRepository.class, "repo-group-memeber"));
        xstream.registerLocalConverter( RepositoryGroupResource.class, "repositories", new AliasingListConverter( RepositoryGroupMemberRepository.class, "repo-group-memeber"));
        xstream.registerLocalConverter( RepositoryGroupListResourceResponse.class, "data", new AliasingListConverter( RepositoryGroupListResource.class, "repo-group-list-item"));
        

        xstream.omitField( RepositoryGroupResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupResource.class, "modelEncoding" );
        xstream.omitField( RepositoryGroupMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-group", RepositoryGroupResourceResponse.class);

        xstream.omitField( RepositoryRouteListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteListResource.class, "modelEncoding" );
        xstream.alias( "repo-routes-list", RepositoryRouteListResourceResponse.class);
//        xstream.alias( "repo-routes-list-item", RepositoryRouteListResource.class);
//        xstream.alias( "repo-routes-member", RepositoryRouteMemberRepository.class);
        xstream.registerLocalConverter( RepositoryRouteListResourceResponse.class, "data", new AliasingListConverter( RepositoryRouteListResource.class, "repo-routes-list-item"));
        xstream.registerLocalConverter( RepositoryRouteListResource.class, "repositories", new AliasingListConverter( RepositoryRouteMemberRepository.class, "repo-routes-member"));


        xstream.omitField( RepositoryRouteResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteResource.class, "modelEncoding" );
        xstream.omitField( RepositoryRouteMemberRepository.class, "modelEncoding" );
        xstream.alias( "repo-route", RepositoryRouteResourceResponse.class);
        xstream.registerLocalConverter( RepositoryRouteResource.class, "repositories", new AliasingListConverter(RepositoryRouteMemberRepository.class, "repository") );

        xstream.omitField( GlobalConfigurationListResourceResponse.class, "modelEncoding" );
        xstream.omitField( GlobalConfigurationListResource.class, "modelEncoding" );
        xstream.alias( "global-settings-list", GlobalConfigurationListResourceResponse.class);
//        xstream.alias( "global-settings-list-item", GlobalConfigurationListResource.class);
        xstream.registerLocalConverter( GlobalConfigurationListResourceResponse.class, "data", new AliasingListConverter( GlobalConfigurationListResource.class, "global-settings-list-item"));
        

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
//        xstream.alias( "logs-list-item", LogsListResource.class);
        xstream.registerLocalConverter( LogsListResourceResponse.class, "data", new AliasingListConverter( LogsListResource.class, "logs-list-item"));
        
        
        xstream.omitField( ConfigurationsListResourceResponse.class, "modelEncoding" );
        xstream.omitField( ConfigurationsListResource.class, "modelEncoding" );
        xstream.alias( "configs-list", ConfigurationsListResourceResponse.class);
//        xstream.alias( "configs-list-tem", ConfigurationsListResource.class);
        xstream.registerLocalConverter( ConfigurationsListResourceResponse.class, "data", new AliasingListConverter( ConfigurationsListResource.class, "configs-list-tem"));
        
        xstream.omitField( FeedListResourceResponse.class, "modelEncoding" );
        xstream.omitField( FeedListResource.class, "modelEncoding" );
        xstream.alias( "feeds-list", FeedListResourceResponse.class);
//        xstream.alias( "feeds-list-item", FeedListResource.class);
        xstream.registerLocalConverter( FeedListResourceResponse.class, "data", new AliasingListConverter( FeedListResource.class, "feeds-list-item"));
        

        xstream.omitField( SearchResponse.class, "modelEncoding" );
        xstream.alias( "search-results", SearchResponse.class);
        xstream.registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class, "artifact"));
        

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
        xstream.registerLocalConverter( StatusConfigurationValidationResponse.class, "validationErrors", new AliasingListConverter( String.class, "error"));
        xstream.registerLocalConverter( StatusConfigurationValidationResponse.class, "validationWarnings", new AliasingListConverter( String.class, "warning"));
        
        
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
//        xstream.alias( "schedules-list-item", ScheduledServiceListResource.class );
        xstream.alias( "scheduled-task", ScheduledServiceResourceResponse.class );
//        xstream.alias( "scheduled-task-property", ScheduledServicePropertyResource.class );
        xstream.alias( "schedule-types", ScheduledServiceTypeResourceResponse.class );
        xstream.alias( "schedule-type", ScheduledServiceTypeResource.class );
//        xstream.alias( "schedule-type-property", ScheduledServiceTypePropertyResource.class );
        xstream.registerLocalConverter( ScheduledServiceBaseResource.class, "properties", new AliasingListConverter( ScheduledServicePropertyResource.class, "scheduled-task-property"));
        xstream.registerLocalConverter( ScheduledServiceWeeklyResource.class, "recurringDay", new AliasingListConverter( String.class, "day"));
        xstream.registerLocalConverter( ScheduledServiceTypeResourceResponse.class, "data", new AliasingListConverter( ScheduledServiceTypeResource.class, "schedule-type"));
        xstream.registerLocalConverter( ScheduledServiceTypeResource.class, "properties", new AliasingListConverter( ScheduledServiceTypePropertyResource.class, "scheduled-task-property"));
        xstream.registerLocalConverter( ScheduledServiceListResourceResponse.class, "data", new AliasingListConverter( ScheduledServiceListResource.class, "schedules-list-item"));
        
        
        xstream.omitField( UserListResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResourceRequest.class, "modelEncoding" );
        xstream.omitField( UserResourceResponse.class, "modelEncoding" );
        xstream.omitField( UserResource.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserForgotPasswordResource.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordRequest.class, "modelEncoding" );
        xstream.omitField( UserChangePasswordResource.class, "modelEncoding" );
        xstream.alias( "users-list", UserListResourceResponse.class );
//        xstream.alias( "users-list-item", UserResource.class );
        xstream.alias( "user-request", UserResourceRequest.class );
        xstream.alias( "user-response", UserResourceResponse.class );
        xstream.alias( "user-forgotpw", UserForgotPasswordRequest.class );
        xstream.alias( "user-changepw", UserChangePasswordRequest.class );
        xstream.registerLocalConverter( UserResource.class, "roles", new AliasingListConverter( String.class, "role"));
        xstream.registerLocalConverter( UserListResourceResponse.class, "data", new AliasingListConverter( UserResource.class, "users-list-item"));
        
        
        xstream.omitField( RoleListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RoleResource.class, "modelEncoding" );
        xstream.omitField( RoleResourceRequest.class, "modelEncoding" );
        xstream.omitField( RoleResourceResponse.class, "modelEncoding" );
        xstream.alias( "roles-list", RoleListResourceResponse.class );
//        xstream.alias( "roles-list-item", RoleResource.class );
        xstream.alias( "role-request", RoleResourceRequest.class );
        xstream.alias( "role-response", RoleResourceResponse.class );
        xstream.registerLocalConverter( RoleListResourceResponse.class, "data", new AliasingListConverter( RoleResource.class, "roles-list-item"));
        xstream.registerLocalConverter( RoleResource.class, "roles", new AliasingListConverter( String.class, "role"));
        xstream.registerLocalConverter( RoleResource.class, "privileges", new AliasingListConverter( String.class, "privilege"));
        
        
        
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
        xstream.aliasField( "methods", PrivilegeBaseResource.class, "method" ); 
        xstream.registerLocalConverter( PrivilegeBaseResource.class, "method", new AliasingListConverter( String.class, "method"));

        

        xstream.omitField( NFCResourceResponse.class, "modelEncoding" );
        xstream.omitField( NFCResource.class, "modelEncoding" );
        xstream.omitField( NFCRepositoryResource.class, "modelEncoding" );
        xstream.alias( "nfc-info", NFCResourceResponse.class );
//        xstream.alias( "nfc-repo-info", NFCRepositoryResource.class );
        xstream.registerLocalConverter( NFCResource.class, "nfcContents", new AliasingListConverter( NFCRepositoryResource.class, "nfc-repo-info"));
        xstream.registerLocalConverter( NFCRepositoryResource.class, "nfcPaths", new AliasingListConverter( String.class, "path"));
        
        
        xstream.omitField( RepositoryTargetListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetListResource.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryTargetResource.class, "modelEncoding" );
        xstream.alias( "repo-targets-list", RepositoryTargetListResourceResponse.class );
//        xstream.alias( "repo-targets-list-item", RepositoryTargetListResource.class );
        xstream.alias( "repo-target", RepositoryTargetResourceResponse.class );
        xstream.registerLocalConverter( RepositoryTargetResource.class, "patterns", new AliasingListConverter( String.class, "pattern"));
        xstream.registerLocalConverter( RepositoryTargetListResourceResponse.class, "data", new AliasingListConverter( RepositoryTargetListResource.class, "repo-targets-list-item"));
        
        
        
        xstream.omitField( RepositoryContentClassListResourceResponse.class, "modelEncoding" );
        xstream.omitField( RepositoryContentClassListResource.class, "modelEncoding" );
        xstream.alias( "repo-content-classes-list", RepositoryContentClassListResourceResponse.class );
//        xstream.alias( "repo-content-classes-list-item", RepositoryContentClassListResource.class );
        xstream.registerLocalConverter( RepositoryContentClassListResourceResponse.class, "data", new AliasingListConverter( RepositoryContentClassListResource.class, "repo-content-classes-list-item"));
        
        
        xstream.omitField( PlexusComponentListResourceResponse.class, "modelEncoding" );
        xstream.omitField( PlexusComponentListResource.class, "modelEncoding" );
        xstream.alias( "components-list", PlexusComponentListResourceResponse.class );
        xstream.alias( "component", PlexusComponentListResource.class );
        xstream.registerLocalConverter( PlexusComponentListResourceResponse.class, "data", new AliasingListConverter( PlexusComponentListResource.class, "component"));
        
        // Maven model
        xstream.omitField( Model.class, "modelEncoding" );
        xstream.omitField( ModelBase.class, "modelEncoding" );
        xstream.omitField( Scm.class, "modelEncoding" );
        return xstream;
    }
}

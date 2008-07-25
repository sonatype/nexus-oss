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
package org.sonatype.nexus.rest.authentication;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.AuthenticationClientPermissions;
import org.sonatype.nexus.rest.model.AuthenticationLoginResource;
import org.sonatype.nexus.rest.model.AuthenticationLoginResourceResponse;

/**
 * The login resource handler. It creates a user token.
 * 
 * @author cstamas
 */
public class LoginResourceHandler
    extends AbstractNexusResourceHandler
{
    private static final int READ = 1;

    private static final int EDIT = 2;

    private static final int DELETE = 4;

    public LoginResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        AuthenticationLoginResource resource = new AuthenticationLoginResource();

        resource.setAuthToken( "DEPRECATED" );

        AuthenticationClientPermissions perms = new AuthenticationClientPermissions();

        perms.setViewSearch( READ );

        perms.setViewUpdatedArtifacts( 0 );

        perms.setViewCachedArtifacts( READ );

        perms.setViewDeployedArtifacts( READ );

        perms.setViewSystemChanges( READ );

        perms.setMaintLogs( READ );

        perms.setMaintConfig( READ );

        perms.setMaintRepos( READ | EDIT );

        perms.setConfigServer( READ | EDIT );

        perms.setConfigGroups( READ | EDIT | DELETE );

        perms.setConfigRules( READ | EDIT | DELETE );

        perms.setConfigRepos( READ | EDIT | DELETE );
        
        perms.setConfigSchedules( READ | EDIT | DELETE );
        
        perms.setConfigUsers( READ | EDIT | DELETE );
        
        perms.setConfigRoles( READ | EDIT | DELETE );
        
        perms.setConfigPrivileges( READ | EDIT | DELETE );
        
        perms.setConfigRepoTargets( READ | EDIT | DELETE );

        resource.setClientPermissions( perms );

        AuthenticationLoginResourceResponse response = new AuthenticationLoginResourceResponse();

        response.setData( resource );

        return serialize( variant, response );
    }

}

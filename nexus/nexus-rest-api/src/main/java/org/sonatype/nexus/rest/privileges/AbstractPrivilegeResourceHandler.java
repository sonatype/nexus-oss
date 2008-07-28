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
package org.sonatype.nexus.rest.privileges;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;

public class AbstractPrivilegeResourceHandler
extends AbstractNexusResourceHandler
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";
    
    public static final String TYPE_APPLICATION = "application";
    public static final String TYPE_REPO_TARGET = "repositoryTarget";

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractPrivilegeResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }
    
    public PrivilegeBaseStatusResource nexusToRestModel( CPrivilege privilege )
    {
        PrivilegeBaseStatusResource resource = null;
        
        if ( CApplicationPrivilege.class.isAssignableFrom( privilege.getClass() ) )
        {
            resource = new PrivilegeApplicationStatusResource();
            
            PrivilegeApplicationStatusResource res = ( PrivilegeApplicationStatusResource ) resource;
            CApplicationPrivilege priv = ( CApplicationPrivilege ) privilege;
            
            res.setPermission( priv.getPermission() );
            res.setType( TYPE_APPLICATION );
        }
        else if ( CRepoTargetPrivilege.class.isAssignableFrom( privilege.getClass() ) )
        {
            resource = new PrivilegeTargetStatusResource();
            
            PrivilegeTargetStatusResource res = ( PrivilegeTargetStatusResource ) resource;
            CRepoTargetPrivilege priv = ( CRepoTargetPrivilege ) privilege;
            
            res.setRepositoryTargetId( priv.getRepositoryTargetId() );
            res.setRepositoryId( priv.getRepositoryId() );
            res.setRepositoryGroupId( priv.getGroupId() );
            
            res.setType( TYPE_REPO_TARGET );
        }
        
        if ( resource != null )
        {
            resource.setId( privilege.getId() );
            resource.setMethod( privilege.getMethod() );
            resource.setName( privilege.getName() );
            resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
        }
                
        return resource;
    }
}

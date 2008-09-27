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

import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.nexus.jsecurity.realms.NexusMethodAuthorizingRealm;
import org.sonatype.nexus.jsecurity.realms.NexusTargetAuthorizingRealm;
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
        
        if ( privilege.getType().equals( NexusMethodAuthorizingRealm.PRIVILEGE_TYPE_METHOD ) )
        {
            resource = new PrivilegeApplicationStatusResource();
            
            PrivilegeApplicationStatusResource res = ( PrivilegeApplicationStatusResource ) resource;
            
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                if ( prop.getKey().equals( NexusMethodAuthorizingRealm.PRIVILEGE_PROPERTY_PERMISSION ) )
                {
                    res.setPermission( prop.getValue() );       
                }
            }
            res.setType( TYPE_APPLICATION );
        }
        else if ( privilege.getType().equals( NexusTargetAuthorizingRealm.PRIVILEGE_TYPE_TARGET ) )
        {
            resource = new PrivilegeTargetStatusResource();
            
            PrivilegeTargetStatusResource res = ( PrivilegeTargetStatusResource ) resource;
            
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                if ( prop.getKey().equals( NexusTargetAuthorizingRealm.PRIVILEGE_PROPERTY_REPOSITORY_TARGET ) )
                {
                    res.setRepositoryTargetId( prop.getValue() );       
                }
                else if ( prop.getKey().equals( NexusTargetAuthorizingRealm.PRIVILEGE_PROPERTY_REPOSITORY_ID ) )
                {
                    res.setRepositoryId( prop.getValue() );        
                }
                else if ( prop.getKey().equals( NexusTargetAuthorizingRealm.PRIVILEGE_PROPERTY_REPOSITORY_GROUP_ID ) )
                {
                    res.setRepositoryGroupId( prop.getValue() );        
                }
            }
                        
            res.setType( TYPE_REPO_TARGET );
        }
        
        if ( resource != null )
        {
            resource.setId( privilege.getId() );
            resource.setName( privilege.getName() );
            resource.setDescription( privilege.getDescription() );
            resource.setResourceURI( calculateSubReference( resource.getId() ).toString() );
            
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                if ( prop.getKey().equals( NexusMethodAuthorizingRealm.PRIVILEGE_PROPERTY_METHOD ) )
                {
                    resource.setMethod( prop.getValue() );       
                }
            }
        }
                
        return resource;
    }
}

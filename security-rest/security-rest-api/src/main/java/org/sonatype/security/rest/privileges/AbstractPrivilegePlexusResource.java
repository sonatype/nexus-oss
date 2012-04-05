/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.privileges;

import java.util.Map.Entry;

import org.restlet.data.Request;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PrivilegeProperty;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public abstract class AbstractPrivilegePlexusResource
    extends AbstractSecurityPlexusResource
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";

    public PrivilegeStatusResource securityToRestModel( Privilege privilege, Request request, boolean appendResourceId )
    {
        PrivilegeStatusResource resource = new PrivilegeStatusResource();
        
        for ( Entry<String, String> prop : privilege.getProperties().entrySet() )
        {
            PrivilegeProperty privProp = new PrivilegeProperty();
            privProp.setKey( prop.getKey() );
            privProp.setValue( prop.getValue() );
            
            resource.addProperty( privProp );
        }
        
        resource.setType( privilege.getType() );
        resource.setId( privilege.getId() );
        resource.setName( privilege.getName() );
        resource.setDescription( privilege.getDescription() );
        
        String resourceId = "";
        if ( appendResourceId )
        {
            resourceId = resource.getId();
        }
        resource.setResourceURI( this.createChildReference( request, resourceId ).toString() );
        
        resource.setUserManaged( !privilege.isReadOnly() );

        return resource;
    }

}

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
package org.sonatype.security.rest.privileges;

import java.util.List;

import org.restlet.data.Request;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.security.rest.AbstractSecurityPlexusResource;
import org.sonatype.security.rest.model.PrivilegeProperty;
import org.sonatype.security.rest.model.PrivilegeStatusResource;

public abstract class AbstractPrivilegePlexusResource
    extends AbstractSecurityPlexusResource
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";

    @SuppressWarnings( "unchecked" )
    public PrivilegeStatusResource nexusToRestModel( SecurityPrivilege privilege, Request request )
    {
        PrivilegeStatusResource resource = new PrivilegeStatusResource();
        
        for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
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
        resource.setResourceURI( createChildReference( request, this, resource.getId() ).toString() );
        resource.setUserManaged( !privilege.isReadOnly() );

        return resource;
    }

}

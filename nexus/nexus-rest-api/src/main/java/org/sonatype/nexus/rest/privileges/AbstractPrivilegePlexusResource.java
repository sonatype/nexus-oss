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
package org.sonatype.nexus.rest.privileges;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegeMethodPropertyDescriptor;
import org.sonatype.jsecurity.realms.privileges.application.ApplicationPrivilegePermissionPropertyDescriptor;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeGroupPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryTargetPropertyDescriptor;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;

public abstract class AbstractPrivilegePlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";

    @Requirement
    private NexusSecurity nexusSecurity;

    protected NexusSecurity getNexusSecurity()
    {
        return nexusSecurity;
    }

    @SuppressWarnings( "unchecked" )
    public PrivilegeBaseStatusResource nexusToRestModel( SecurityPrivilege privilege, Request request )
    {
        PrivilegeBaseStatusResource resource = null;

        if ( privilege.getType().equals( ApplicationPrivilegeDescriptor.TYPE ) )
        {
            resource = new PrivilegeApplicationStatusResource();

            PrivilegeApplicationStatusResource res = (PrivilegeApplicationStatusResource) resource;

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
            {
                if ( prop.getKey().equals( ApplicationPrivilegePermissionPropertyDescriptor.ID ) )
                {
                    res.setPermission( prop.getValue() );
                }
            }
        }
        else if ( privilege.getType().equals( TargetPrivilegeDescriptor.TYPE ) )
        {
            resource = new PrivilegeTargetStatusResource();

            PrivilegeTargetStatusResource res = (PrivilegeTargetStatusResource) resource;

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
            {
                if ( prop.getKey().equals( TargetPrivilegeRepositoryTargetPropertyDescriptor.ID ) )
                {
                    res.setRepositoryTargetId( prop.getValue() );
                }
                else if ( prop.getKey().equals( TargetPrivilegeRepositoryPropertyDescriptor.ID ) )
                {
                    res.setRepositoryId( prop.getValue() );
                }
                else if ( prop.getKey().equals( TargetPrivilegeGroupPropertyDescriptor.ID ) )
                {
                    res.setRepositoryGroupId( prop.getValue() );
                }
            }
        }

        if ( resource != null )
        {
            resource.setType( privilege.getType() );
            resource.setId( privilege.getId() );
            resource.setName( privilege.getName() );
            resource.setDescription( privilege.getDescription() );
            resource.setResourceURI( this.createChildReference( request, resource.getId() ).toString() );
            resource.setUserManaged( !privilege.isReadOnly() );

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
            {
                if ( prop.getKey().equals( ApplicationPrivilegeMethodPropertyDescriptor.ID ) )
                {
                    resource.setMethod( prop.getValue() );
                }
            }
        }

        return resource;
    }

}

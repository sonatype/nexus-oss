/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.privileges;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.sonatype.jsecurity.model.CProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.jsecurity.realms.NexusMethodAuthorizingRealm;
import org.sonatype.nexus.jsecurity.realms.NexusTargetAuthorizingRealm;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.PrivilegeApplicationStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetStatusResource;

public abstract class AbstractPrivilegePlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String PRIVILEGE_ID_KEY = "privilegeId";

    public static final String TYPE_APPLICATION = "application";

    public static final String TYPE_REPO_TARGET = "repositoryTarget";

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

        if ( privilege.getType().equals( NexusMethodAuthorizingRealm.PRIVILEGE_TYPE_METHOD ) )
        {
            resource = new PrivilegeApplicationStatusResource();

            PrivilegeApplicationStatusResource res = (PrivilegeApplicationStatusResource) resource;

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
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

            PrivilegeTargetStatusResource res = (PrivilegeTargetStatusResource) resource;

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
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
            resource.setResourceURI( this.createChildReference( request, resource.getId() ).toString() );
            resource.setUserManaged( !privilege.isReadOnly() );

            for ( CProperty prop : (List<CProperty>) privilege.getProperties() )
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

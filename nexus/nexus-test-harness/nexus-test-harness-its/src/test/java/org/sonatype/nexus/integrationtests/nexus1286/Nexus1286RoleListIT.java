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
package org.sonatype.nexus.integrationtests.nexus1286;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.security.rest.model.ExternalRoleMappingResource;
import org.sonatype.security.rest.model.PlexusRoleResource;

public class Nexus1286RoleListIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void invalidSource()
        throws IOException
    {
        String uriPart = RequestFacade.SERVICE_LOCAL + "plexus_roles/" + "INVALID";

        Response response = RequestFacade.doGetRequest( uriPart );

        Assert.assertEquals( 404, response.getStatus().getCode() );

    }

    @Test
    public void defaultSourceRoles()
        throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusRoleResource> roles = roleUtil.getRoles( "default" );

        Set<String> ids = this.getRoleIds( roles );
        Assert.assertTrue( ids.contains( "admin" ) );
        Assert.assertTrue( ids.contains( "anonymous" ) );

    }

    @Test
    public void allSourceRoles()
        throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<PlexusRoleResource> roles = roleUtil.getRoles( "all" );

        Set<String> ids = this.getRoleIds( roles );
        Assert.assertTrue( ids.contains( "admin" ) );
        Assert.assertTrue( ids.contains( "anonymous" ) );
    }

    public void getdefaultExternalRoleMap()
        throws IOException
    {
        RoleMessageUtil roleUtil = new RoleMessageUtil( this, this.getJsonXStream(), MediaType.APPLICATION_JSON );
        List<ExternalRoleMappingResource> roles = roleUtil.getExternalRoleMap( "all" );
        Assert.assertEquals( 0, roles.size() );

        roles = roleUtil.getExternalRoleMap( "default" );
        Assert.assertEquals( 0, roles.size() );
    }

    private Set<String> getRoleIds( List<PlexusRoleResource> roles )
    {
        Set<String> ids = new HashSet<String>();
        for ( PlexusRoleResource role : roles )
        {
            ids.add( role.getRoleId() );
        }
        return ids;
    }

}

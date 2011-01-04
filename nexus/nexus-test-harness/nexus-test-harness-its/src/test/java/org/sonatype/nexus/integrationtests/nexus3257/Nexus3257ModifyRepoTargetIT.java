/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3257;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.rest.model.PrivilegeResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.PrivilegesMessageUtil;
import org.sonatype.nexus.test.utils.TargetMessageUtil;
import org.sonatype.security.rest.model.PrivilegeStatusResource;
import org.testng.annotations.Test;

public class Nexus3257ModifyRepoTargetIT
    extends AbstractNexusIntegrationTest
{
    TargetMessageUtil targetUtil;
    PrivilegesMessageUtil privUtil;
    
    public Nexus3257ModifyRepoTargetIT()
    {
        targetUtil = new TargetMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        privUtil = new PrivilegesMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }
    
    @Test
    public void testChangeTarget()
        throws Exception
    {
        RepositoryTargetResource target = new RepositoryTargetResource();
        target.setContentClass( "maven2" );
        target.setName( "nexus3257-target" );
        target.addPattern( "/some-pattern" );        
        
        target = targetUtil.createTarget( target );
        
        // now add some privs
        PrivilegeResource privReq = new PrivilegeResource();
        privReq.setDescription( "nexus3257-target repo-target privilege" );
        privReq.setMethod( Arrays.asList( "create", "read", "update", "delete" ) );
        privReq.setName( "nexus-3257-priv" );
        privReq.setRepositoryTargetId( target.getId() );
        privReq.setType( TargetPrivilegeDescriptor.TYPE );
        
        List<PrivilegeStatusResource> privs = privUtil.createPrivileges( privReq );
        
        // now make sure the privs exist
        checkPrivs( privs );
        
        // now lets change the target and add a new path
        target.addPattern( "/other-pattern" );
        targetUtil.saveTarget( target, true );
        
        // now make sure the privs still exist
        checkPrivs( privs );
    }
    
    private void checkPrivs( List<PrivilegeStatusResource> privs )
        throws Exception
    {
        for ( PrivilegeStatusResource priv : privs )
        {
            privUtil.getPrivilegeResource( priv.getId() );
        }
    }
}

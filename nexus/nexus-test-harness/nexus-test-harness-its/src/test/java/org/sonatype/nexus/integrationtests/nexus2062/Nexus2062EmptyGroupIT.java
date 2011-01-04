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
package org.sonatype.nexus.integrationtests.nexus2062;


import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2062EmptyGroupIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void createEmptyGroup()
        throws Exception
    {
        GroupMessageUtil groupUtil = new GroupMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        
        RepositoryGroupResource resource = new RepositoryGroupResource();
        resource.setExposed( true );
        resource.setFormat( "maven2" );
        resource.setId( "emptygroup" );
        resource.setName( "emptygroup" );
        resource.setProvider( "maven2" );
        
        resource = groupUtil.createGroup( resource );
        
        Assert.assertEquals( 0, resource.getRepositories().size() );
    }
    
    @Test
    public void createGroupWithRepoAndDelete()
        throws Exception
    {
        GroupMessageUtil groupUtil = new GroupMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        
        RepositoryGroupResource resource = new RepositoryGroupResource();
        resource.setExposed( true );
        resource.setFormat( "maven2" );
        resource.setId( "nonemptygroup" );
        resource.setName( "nonemptygroup" );
        resource.setProvider( "maven2" );
        
        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( REPO_TEST_HARNESS_REPO );
        resource.addRepository( member );
        
        resource = groupUtil.createGroup( resource );
        
        resource.getRepositories().clear();
        
        resource = groupUtil.updateGroup( resource );
        
        Assert.assertEquals( 0, resource.getRepositories().size() );
    }
}

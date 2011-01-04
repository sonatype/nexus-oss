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
package org.sonatype.nexus.integrationtests.nexus977;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.nexus532.Nexus532GroupsCrudXmlIT;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsCrudIT
    extends Nexus532GroupsCrudXmlIT
{

    @Override
    protected void createMembers( RepositoryGroupResource resource )
    {
        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( REPO_TEST_HARNESS_REPO );
        resource.addRepository( member );

        member = new RepositoryGroupMemberRepository();
        member.setId( REPO_NEXUS_TEST_HARNESS_RELEASE_GROUP );
        resource.addRepository( member );
    }

    @Test
    public void cyclic()
        throws Exception
    {
        RepositoryGroupResource groupA = new RepositoryGroupResource();

        groupA.setId( "groupA" );
        groupA.setName( "groupA" );
        groupA.setFormat( "maven2" );
        groupA.setProvider( "maven2" );

        createMembers( groupA );

        this.messageUtil.createGroup( groupA );

        RepositoryGroupResource groupB = new RepositoryGroupResource();

        groupB.setId( "groupB" );
        groupB.setName( "groupB" );
        groupB.setFormat( "maven2" );
        groupB.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( groupA.getId() );
        groupB.addRepository( member );

        this.messageUtil.createGroup( groupB );

        // introduces cyclic referece between repos
        member = new RepositoryGroupMemberRepository();
        member.setId( groupB.getId() );
        groupA.addRepository( member );
        Response resp = this.messageUtil.sendMessage( Method.PUT, groupA );
        Assert.assertFalse( resp.getStatus().isSuccess() );
    }
}

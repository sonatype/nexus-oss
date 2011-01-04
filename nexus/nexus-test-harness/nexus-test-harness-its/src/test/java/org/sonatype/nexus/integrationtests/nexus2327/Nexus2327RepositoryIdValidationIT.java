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
package org.sonatype.nexus.integrationtests.nexus2327;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2327RepositoryIdValidationIT
    extends AbstractNexusIntegrationTest
{
    private RepositoryMessageUtil repositoryMsgUtil;

    private GroupMessageUtil groupMsgUtil;

    public Nexus2327RepositoryIdValidationIT()
        throws Exception
    {
        repositoryMsgUtil = new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );

        groupMsgUtil = new GroupMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void repositoryIdLegal()
        throws Exception
    {
        RepositoryResource resource = new RepositoryResource();
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );

        resource.setId( "repoaA1-_." );
        Response resp = repositoryMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertTrue( resp.getStatus().isSuccess() );
    }

    @Test
    public void repositoryIdIllegal()
        throws Exception
    {
        RepositoryResource resource = new RepositoryResource();
        resource.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        resource.setName( "Create Test Repo" );
        resource.setProvider( "maven2" );
        resource.setFormat( "maven2" );
        resource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );

        resource.setId( "repo/" );
        Response resp = repositoryMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "repo," );
        resp = repositoryMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "repo*" );
        resp = repositoryMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "repo>" );
        resp = repositoryMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );
    }

    @Test
    public void groupIdLegal()
        throws Exception
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();
        resource.setName( "createTestGroup" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );
        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        resource.setId( "groupaA0-_." );
        Response resp = groupMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertTrue( resp.getStatus().isSuccess() );
    }

    @Test
    public void groupIdIllegal()
        throws Exception
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setName( "createTestGroup" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );
        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        resource.setId( "group/" );
        Response resp = groupMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "group," );
        resp = groupMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "group*" );
        resp = groupMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );

        resource.setId( "group>" );
        resp = groupMsgUtil.sendMessage( Method.POST, resource );
        Assert.assertFalse( resp.getStatus().isSuccess() );
    }

}

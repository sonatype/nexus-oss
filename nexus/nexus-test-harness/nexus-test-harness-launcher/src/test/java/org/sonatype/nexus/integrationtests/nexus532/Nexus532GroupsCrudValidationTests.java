/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.integrationtests.nexus532;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;

public class Nexus532GroupsCrudValidationTests
    extends AbstractNexusIntegrationTest
{

    protected GroupMessageUtil messageUtil;

    public Nexus532GroupsCrudValidationTests()
    {
        this.messageUtil =
            new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void noIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        // resource.setId( "noIdTest" );
        resource.setName( "noIdTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

    @Test
    public void emptyIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "" );
        resource.setName( "emptyIdTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

    @Test
    public void noNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noNameTest" );
        // resource.setName( "noNameTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Group should have been created: " + response.getStatus() + "\n" + responseText,
                           response.getStatus().isSuccess() );

        // check if the created group Name == the id
        Assert.assertEquals( "Group Name did not default to the Id", resource.getId(),
                             this.messageUtil.getGroup( resource.getId() ).getName() );
    }

    @Test
    public void emptyNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "emptyNameTest" );
        resource.setName( "" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( "Group should have been created: " + response.getStatus() + "\n" + responseText,
                           response.getStatus().isSuccess() );

        // check if the created group Name == the id
        Assert.assertEquals( "Group Name did not default to the Id", resource.getId(),
                             this.messageUtil.getGroup( resource.getId() ).getName() );
    }

    @Test
    public void maven2Maven2GroupTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "maven2Maven2GroupTest" );
        resource.setName( "maven2Maven2GroupTest" );

        RepositoryGroupMemberRepository m2Repo = new RepositoryGroupMemberRepository();
        m2Repo.setId( "nexus-test-harness-repo" );
        resource.addRepository( m2Repo );
        
        RepositoryGroupMemberRepository m1Repo = new RepositoryGroupMemberRepository();
        m1Repo.setId( "nexus-test-harness-shadow" );
        resource.addRepository( m1Repo );
        

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();
        // should fail
        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

    @Test
    public void noRepos()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noRepos" );
        resource.setName( "noRepos" );
        resource.setFormat( "maven2" );

        // RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        // member.setId( "nexus-test-harness-repo" );
        // resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

    @Test
    public void invalidRepoId()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "invalidRepoId" );
        resource.setName( "invalidRepoId" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "really-invalid-repo-name" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been created: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
    }

    @Test
    public void updateValidationTest()
        throws IOException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "updateValidationTest" );
        resource.setName( "updateValidationTest" );
        resource.setFormat( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        resource = this.messageUtil.createGroup( resource );

        Response response = null;
        String responseText = null;

       

        // no groups
        resource.getRepositories().clear();
        response = this.messageUtil.sendMessage( Method.PUT, resource );
        responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been updated: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
        resource.addRepository( member );

        // missing Id
        resource.setId( null );
        response = this.messageUtil.sendMessage( Method.PUT, resource, "updateValidationTest" );
        responseText = response.getEntity().getText();

        Assert.assertFalse( "Group should not have been udpated: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertTrue( "Response text did not contain an error message. Status: " + response.getStatus()
            + "\nResponse Text:\n " + responseText, responseText.contains( "<errors>" ) );
        resource.setId( "updateValidationTest" );
        
        // missing name
        resource.setName( null );
        response = this.messageUtil.sendMessage( Method.PUT, resource );
        responseText = response.getEntity().getText();

        Assert.assertTrue( "Group should have been udpated: " + response.getStatus() + "\n" + responseText,
                            response.getStatus().isSuccess() );
        Assert.assertEquals( "Group Name did not default to the Id", resource.getId(),
            this.messageUtil.getGroup( resource.getId() ).getName() );

    }

}

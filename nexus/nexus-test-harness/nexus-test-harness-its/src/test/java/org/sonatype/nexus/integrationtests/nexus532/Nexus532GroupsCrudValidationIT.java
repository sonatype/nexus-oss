/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus532;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus532GroupsCrudValidationIT
    extends AbstractNexusIntegrationTest
{

    protected GroupMessageUtil messageUtil;

    public Nexus532GroupsCrudValidationIT()
    {
        this.messageUtil = new GroupMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void noIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        // resource.setId( "noIdTest" );
        resource.setName( "noIdTest" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( response.getStatus().isSuccess(),
                            "Group should not have been created: " + response.getStatus() + "\n" + responseText );
        Assert.assertTrue( responseText.contains( "<errors>" ),
                           "Response text did not contain an error message. Status: " + response.getStatus()
                               + "\nResponse Text:\n " + responseText );
    }

    @Test
    public void emptyIdTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "" );
        resource.setName( "emptyIdTest" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( response.getStatus().isSuccess(),
                            "Group should not have been created: " + response.getStatus() + "\n" + responseText );
        Assert.assertTrue( responseText.contains( "<errors>" ),
                           "Response text did not contain an error message. Status: " + response.getStatus()
                               + "\nResponse Text:\n " + responseText );
    }

    @Test
    public void noNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noNameTest" );
        // resource.setName( "noNameTest" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( response.getStatus().isSuccess(), "Group should have been created: " + response.getStatus()
            + "\n" + responseText );

        // check if the created group Name == the id
        Assert.assertEquals( this.messageUtil.getGroup( resource.getId() ).getName(), resource.getId(),
                             "Group Name did not default to the Id" );
    }

    @Test
    public void emptyNameTest()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "emptyNameTest" );
        resource.setName( "" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "nexus-test-harness-repo" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( response.getStatus().isSuccess(), "Group should have been created: " + response.getStatus()
            + "\n" + responseText );

        // check if the created group Name == the id
        Assert.assertEquals( this.messageUtil.getGroup( resource.getId() ).getName(), resource.getId(),
                             "Group Name did not default to the Id" );
    }

    @Test
    public void maven1Maven2GroupTest()
        throws IOException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "maven2Maven2GroupTest" );
        resource.setName( "maven2Maven2GroupTest" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository m2Repo = new RepositoryGroupMemberRepository();
        m2Repo.setId( "nexus-test-harness-repo" );
        resource.addRepository( m2Repo );

        RepositoryGroupMemberRepository m1Repo = new RepositoryGroupMemberRepository();
        m1Repo.setId( "nexus-test-harness-shadow" );
        resource.addRepository( m1Repo );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();
        // should fail
        Assert.assertFalse( response.getStatus().isSuccess(),
                            "Group should not have been created: " + response.getStatus() + "\n" + responseText );
        Assert.assertTrue( responseText.contains( "<errors>" ),
                           "Response text did not contain an error message. Status: " + response.getStatus()
                               + "\nResponse Text:\n " + responseText );
    }

    @Test
    public void noRepos()
        throws IOException
    {

        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "noRepos" );
        resource.setName( "noRepos" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        // RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        // member.setId( "nexus-test-harness-repo" );
        // resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertTrue( response.getStatus().isSuccess(), "Group should have been created: " + response.getStatus()
            + "\n" + responseText );
    }

    @Test
    public void invalidRepoId()
        throws IOException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "invalidRepoId" );
        resource.setName( "invalidRepoId" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

        RepositoryGroupMemberRepository member = new RepositoryGroupMemberRepository();
        member.setId( "really-invalid-repo-name" );
        resource.addRepository( member );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );
        String responseText = response.getEntity().getText();

        Assert.assertFalse( response.getStatus().isSuccess(),
                            "Group should not have been created: " + response.getStatus() + "\n" + responseText );
        Assert.assertTrue( responseText.contains( "<errors>" ),
                           "Response text did not contain an error message. Status: " + response.getStatus()
                               + "\nResponse Text:\n " + responseText );
    }

    @Test
    public void updateValidationTest()
        throws IOException
    {
        RepositoryGroupResource resource = new RepositoryGroupResource();

        resource.setId( "updateValidationTest" );
        resource.setName( "updateValidationTest" );
        resource.setFormat( "maven2" );
        resource.setProvider( "maven2" );

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

        Assert.assertTrue( response.getStatus().isSuccess(), "Group should have been updated: " + response.getStatus()
            + "\n" + responseText );
        resource.addRepository( member );

        // missing Id
        resource.setId( null );
        response = this.messageUtil.sendMessage( Method.PUT, resource, "updateValidationTest" );
        responseText = response.getEntity().getText();

        Assert.assertFalse( response.getStatus().isSuccess(),
                            "Group should not have been udpated: " + response.getStatus() + "\n" + responseText );
        Assert.assertTrue( responseText.contains( "<errors>" ),
                           "Response text did not contain an error message. Status: " + response.getStatus()
                               + "\nResponse Text:\n " + responseText );
        resource.setId( "updateValidationTest" );

        // missing name
        resource.setName( null );
        response = this.messageUtil.sendMessage( Method.PUT, resource );
        responseText = response.getEntity().getText();

        Assert.assertTrue( response.getStatus().isSuccess(), "Group should have been udpated: " + response.getStatus()
            + "\n" + responseText );
        Assert.assertEquals( this.messageUtil.getGroup( resource.getId() ).getName(), resource.getId(),
                             "Group Name did not default to the Id" );

    }

}

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
package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;

/**
 * Extra CRUD validation tests.
 */
public class Nexus385RoutesValidationTests
    extends AbstractNexusIntegrationTest
{

    protected RoutesMessageUtil messageUtil;

    public Nexus385RoutesValidationTests()
    {
        this.messageUtil = new RoutesMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void createNoGroupIdTest()
        throws IOException
    {
        // EXPLANATION
        // When no groupId sent with route, Nexus _defaults_ it to '*', meaning
        // all repositories to "mimic" the pre-this-change behaviour

        RepositoryRouteResource resource = new RepositoryRouteResource();
        // resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*createNoGroupIdTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 201 || !responseText.contains( "<groupId>*</groupId>" ) )
        {
            Assert.fail( "Should have returned a 201, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText + ", and the omitted groupId should be defaulted with '*'" );
        }
    }

    @Test
    public void createNoRuleTypeTest()
        throws IOException
    {

        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*createNoRuleTypeTest.*" );
        // resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @Test
    public void createNoPatternTest()
        throws IOException
    {
        if( this.printKnownErrorButDoNotFail( this.getClass(), "createNoPatternTest" ) )
        {
            return;
        }
        
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        // resource.setPattern( ".*createNoPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @Test
    public void createWithInvalidPatternTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );

    }

    @Test
    public void createWithInvalidGroupTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "INVALID" );
        resource.setPattern( "*.createWithInvalidPatternTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

    @Test
    public void createWithInvalidRuleTypeTest()
        throws IOException
    {

        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "createWithInvalidRuleTypeTest" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "nexus-test-harness-repo" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

    @Test
    public void createNoReposTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "exclusive" );

        // RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        // memberRepo1.setId( "nexus-test-harness-repo" );
        // resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

    @Test
    public void createWithInvalidReposTest()
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( "*.createWithInvalidRuleTypeTest.*" );
        resource.setRuleType( "exclusive" );

        RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
        memberRepo1.setId( "INVALID" );
        resource.addRepository( memberRepo1 );

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().getCode() != 400 )
        {
            Assert.fail( "Should have returned a 400, but returned: " + response.getStatus() + "\nresponse:\n"
                + responseText );
        }

        this.messageUtil.validateResponseErrorXml( responseText );
    }

}

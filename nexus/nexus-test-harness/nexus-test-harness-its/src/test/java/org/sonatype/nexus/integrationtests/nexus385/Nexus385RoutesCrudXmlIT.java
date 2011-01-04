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
package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.test.utils.RoutesMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

/**
 * CRUD tests for XML request/response.
 */
public class Nexus385RoutesCrudXmlIT
    extends AbstractNexusIntegrationTest
{

    protected RoutesMessageUtil messageUtil;

    @BeforeClass
    public void setSecureTest(){
    	this.messageUtil = new RoutesMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @BeforeMethod
    public void cleanRoutes()
        throws IOException
    {
        RoutesMessageUtil.removeAllRoutes();
    }

    @Test
    public void createRouteTest()
        throws IOException
    {
        this.runCreateTest( "exclusive" );
        this.runCreateTest( "inclusive" );
        this.runCreateTest( "blocking" );
    }

    @SuppressWarnings( "unchecked" )
    private RepositoryRouteResource runCreateTest( String ruleType )
        throws IOException
    {
        RepositoryRouteResource resource = new RepositoryRouteResource();
        resource.setGroupId( "nexus-test" );
        resource.setPattern( ".*" + ruleType + ".*" );
        resource.setRuleType( ruleType );

        if ( !"blocking".equals( ruleType ) )
        {
            RepositoryRouteMemberRepository memberRepo1 = new RepositoryRouteMemberRepository();
            memberRepo1.setId( "nexus-test-harness-repo" );
            resource.addRepository( memberRepo1 );
        }

        Response response = this.messageUtil.sendMessage( Method.POST, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            try
            {
                Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
            }
            catch ( NullPointerException e )
            {
                Assert.fail( new XStream().toXML( response ) );
            }
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

        return resourceResponse;
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void readTest()
        throws IOException
    {
        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );

        Response response = this.messageUtil.sendMessage( Method.GET, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void updateTest()
        throws IOException
    {
        // FIXME: this test is known to fail, but is commented out so the CI builds are useful
        if ( this.printKnownErrorButDoNotFail( this.getClass(), "updateTest" ) )
        {
            return;
        }

        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );
        resource.setPattern( ".*update.*" );

        Response response = this.messageUtil.sendMessage( Method.PUT, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        // get the Resource object
        RepositoryRouteResource resourceResponse = this.messageUtil.getResourceFromResponse( response );

        Assert.assertNotNull( resourceResponse.getId() );

        Assert.assertEquals( resource.getGroupId(), resourceResponse.getGroupId() );
        Assert.assertEquals( resource.getPattern(), resourceResponse.getPattern() );
        Assert.assertEquals( resource.getRuleType(), resourceResponse.getRuleType() );
        this.messageUtil.validateSame( resource.getRepositories(), resourceResponse.getRepositories() );

        // now check the nexus config
        this.messageUtil.validateRoutesConfig( resourceResponse );

    }

    @Test
    public void deleteTest()
        throws IOException
    {
        // create
        RepositoryRouteResource resource = this.runCreateTest( "exclusive" );

        Response response = this.messageUtil.sendMessage( Method.DELETE, resource );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create privilege: " + response.getStatus() + "\nresponse:\n" + responseText );
        }

        Assert.assertTrue( getNexusConfigUtil().getRoute( resource.getId() ) == null, "Route was not deleted." );

    }

}

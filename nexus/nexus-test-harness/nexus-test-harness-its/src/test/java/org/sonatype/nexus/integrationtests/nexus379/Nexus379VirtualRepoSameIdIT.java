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
package org.sonatype.nexus.integrationtests.nexus379;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test to make sure a Virtual repo cannot have the same Id as an real repository.
 */
public class Nexus379VirtualRepoSameIdIT
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil messageUtil;

    @BeforeClass
    public void setSecureTest()
        throws ComponentLookupException
    {
        this.messageUtil = new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void testVirtualRepoWithSameId()
        throws IOException
    {

        // create a repository
        RepositoryResource repo = new RepositoryResource();

        repo.setId( "testVirtualRepoWithSameId" );
        repo.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repo.setName( "testVirtualRepoWithSameId" );
        repo.setProvider( "maven2" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo = (RepositoryResource) this.messageUtil.createRepository( repo );

        // now create a virtual one, this should fail

        // create a repository
        RepositoryShadowResource virtualRepo = new RepositoryShadowResource();

        virtualRepo.setId( "testVirtualRepoWithSameId" );
        virtualRepo.setRepoType( "virtual" ); // [hosted, proxy, virtual]
        virtualRepo.setName( "testVirtualRepoWithSameId" );
        virtualRepo.setProvider( "m2-m1-shadow" );
        // format is neglected by server from now on, provider is the new guy in the town
        virtualRepo.setFormat( "maven1" );
        virtualRepo.setShadowOf( "testVirtualRepoWithSameId" );
        Response response = this.messageUtil.sendMessage( Method.POST, virtualRepo );

        Assert.assertEquals( response.getStatus().getCode(), 400, "Status:" + "\n" + response.getEntity().getText() );

    }

}

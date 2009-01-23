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
package org.sonatype.nexus.integrationtests.nexus379;

import java.io.IOException;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

/**
 * Test to make sure a Virtual repo cannot have the same Id as an real repository.
 */
public class Nexus379VirtualRepoSameId
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil messageUtil;

    public Nexus379VirtualRepoSameId()
        throws ComponentLookupException
    {
        this.messageUtil = new RepositoryMessageUtil(
            this.getXMLXStream(),
            MediaType.APPLICATION_XML,
            getRepositoryTypeRegistry() );
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
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "release" );
        repo.setChecksumPolicy( "ignore" ); // [ignore, warn, strictIfExists, strict]
        repo = (RepositoryResource) this.messageUtil.createRepository( repo );

        // now create a virtual one, this should fail

        // create a repository
        RepositoryShadowResource virtualRepo = new RepositoryShadowResource();

        virtualRepo.setId( "testVirtualRepoWithSameId" );
        virtualRepo.setRepoType( "virtual" ); // [hosted, proxy, virtual]
        virtualRepo.setName( "testVirtualRepoWithSameId" );
        virtualRepo.setFormat( "maven1" );
        virtualRepo.setShadowOf( "testVirtualRepoWithSameId" );
        Response response = this.messageUtil.sendMessage( Method.POST, virtualRepo );

        Assert.assertEquals( "Status:" + "\n" + response.getEntity().getText(), 400, response.getStatus().getCode() );

    }

}

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
package org.sonatype.nexus.integrationtests.nexus2991;

import java.util.List;

import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author juven
 */
public class Nexus2991DeleteRepositoryBeforeSearchIT
    extends AbstractPrivilegeTest
{
    private NexusClient getConnectedNexusClient()
        throws Exception
    {
        NexusClient client = (NexusClient) lookup( NexusClient.ROLE );
        TestContext context = TestContainer.getInstance().getTestContext();
        client.connect( AbstractNexusIntegrationTest.nexusBaseUrl, context.getAdminUsername(), context
            .getAdminPassword() );

        return client;
    }

    @Test
    public void searchBeforeAndAfterDeletingRepository()
        throws Exception
    {
        NexusClient client = this.getConnectedNexusClient();
        NexusArtifact searchParam = new NexusArtifact();
        searchParam.setArtifactId( "nexus2991-artifact" );
        searchParam.setGroupId( "nexus2991" );
        searchParam.setVersion( "1.0.1" );
        searchParam.setPackaging( "jar" );
        searchParam.setClassifier( null );
        List<NexusArtifact> results = client.searchByGAV( searchParam );
        Assert.assertEquals( results.size(), 1, "Search result size" );
        client.disconnect();

        // create a repo
        client = this.getConnectedNexusClient();
        RepositoryResource repoResoruce = new RepositoryResource();
        repoResoruce.setId( "testCreate" );
        repoResoruce.setRepoType( "hosted" ); // [hosted, proxy, virtual]
        repoResoruce.setName( "Create Test Repo" );
        repoResoruce.setProvider( "maven2" );
        repoResoruce.setProviderRole( Repository.class.getName() );
        repoResoruce.setFormat( "maven2" ); // Repository Format, maven1, maven2, maven-site, eclipse-update-site
        repoResoruce.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repoResoruce.setBrowseable( true );
        repoResoruce.setIndexable( true );
        repoResoruce.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repoResoruce.setChecksumPolicy( "IGNORE" ); // [ignore, warn, strictIfExists, strict]
        RepositoryBaseResource repoResult = client.createRepository( repoResoruce );
        Assert.assertNotNull( repoResult );
        RepositoryBaseResource repoExpected = client.getRepository( "testCreate" );
        Assert.assertEquals( repoResult.getId(), repoExpected.getId() );
        Assert.assertEquals( repoResult.getName(), repoExpected.getName() );
        Assert.assertEquals( repoResult.getFormat(), repoExpected.getFormat() );
        // delete it
        client.deleteRepository( "testCreate" );
        client.disconnect();

        // give some time to nexus for update index
        Thread.sleep( 1000 );

        // search again
        client = this.getConnectedNexusClient();
        results = client.searchByGAV( searchParam );
        Assert.assertEquals( results.size(), 1, "Search result size" );
        client.disconnect();
    }
}

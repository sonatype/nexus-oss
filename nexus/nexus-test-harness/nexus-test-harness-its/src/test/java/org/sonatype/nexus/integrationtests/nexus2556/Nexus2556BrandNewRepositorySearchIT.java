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
package org.sonatype.nexus.integrationtests.nexus2556;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus2556BrandNewRepositorySearchIT
    extends AbstractNexusIntegrationTest
{

    private RepositoryMessageUtil repoUtil;

    public Nexus2556BrandNewRepositorySearchIT()
    {

    }

    @BeforeClass
    public void init()
        throws ComponentLookupException
    {
        this.repoUtil = new RepositoryMessageUtil( this, XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML );
    }

    @Test
    public void hostedTest()
        throws IOException, Exception
    {
        String repoId = "nexus2556-hosted";
        RepositoryResource repo = new RepositoryResource();
        repo.setProvider( "maven2" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "release" );
        repo.setChecksumPolicy( "ignore" );
        repo.setBrowseable( false );

        repo.setId( repoId );
        repo.setName( repoId );
        repo.setRepoType( "hosted" );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo.setDownloadRemoteIndexes( true );
        repo.setBrowseable( true );
        repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repo.setChecksumPolicy( ChecksumPolicy.IGNORE.name() );

        repo.setIndexable( true ); // being sure!!!
        repoUtil.createRepository( repo );

        repo = (RepositoryResource) repoUtil.getRepository( repoId );
        Assert.assertTrue( repo.isIndexable() );

        TaskScheduleUtil.waitForAllTasksToStop();
        getEventInspectorsUtil().waitForCalmPeriod();

        Gav gav = GavUtil.newGav( "nexus2556", "artifact", "1.0" );
        getDeployUtils().deployUsingGavWithRest( repoId, gav, getTestFile( "artifact.jar" ) );

        TaskScheduleUtil.waitForAllTasksToStop();
        getEventInspectorsUtil().waitForCalmPeriod();

        List<NexusArtifact> result = getSearchMessageUtil().searchForGav( gav, repoId );
        Assert.assertEquals( result.size(), 1, "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ) );

        result = getSearchMessageUtil().searchFor( Collections.singletonMap( "q", "nexus2556" ), repoId );
        Assert.assertEquals( result.size(), 1, "Results: \n" + XStreamFactory.getXmlXStream().toXML( result ) );
    }

}

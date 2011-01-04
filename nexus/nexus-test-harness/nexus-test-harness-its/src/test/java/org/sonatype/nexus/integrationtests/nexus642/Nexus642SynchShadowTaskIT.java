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
package org.sonatype.nexus.integrationtests.nexus642;

import java.io.File;
import java.net.URL;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.SynchronizeShadowTaskDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus642SynchShadowTaskIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void synchShadowTest()
        throws Exception
    {
        // create shadow repo 'nexus-shadow-repo'
        RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        String shadowRepoId = "nexus-shadow-repo";
        String taskName = "synchShadowTest";

        RepositoryShadowResource repo = new RepositoryShadowResource();
        repo.setId( shadowRepoId );
        repo.setProvider( "m2-m1-shadow" );
        // format is neglected by server from now on, provider is the new guy in the town
        repo.setFormat( "maven1" );
        repo.setName( shadowRepoId );
        repo.setRepoType( "virtual" );
        repo.setShadowOf( this.getTestRepositoryId() );
        repo.setSyncAtStartup( false );
        repo.setExposed( true );
        repoUtil.createRepository( repo );

        // create Sync Repo Task
        // repo: 'nexus-shadow-repo'
        // recurrence: 'manual'
        // run it manually
        this.executeTask( taskName, repo.getId() );

        // download the file using the shadow repo
        File actualFile =
            this.downloadFile(
                new URL( this.getBaseNexusUrl() + "content/repositories/" + shadowRepoId + "/" + this.getTestId()
                    + "/jars/artifact-5.4.3.jar" ), "target/downloads/nexus642.jar" );
        File expectedFile = this.getTestResourceAsFile( "projects/artifact/artifact.jar" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( expectedFile, actualFile ) );

    }

    private void executeTask( String taskName, String shadowRepo )
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "sync-repo-props" );
        repo.setValue( shadowRepo );

        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setKey( "shadowRepositoryId" );
        age.setValue( shadowRepo );

        // clean unused
        TaskScheduleUtil.runTask( taskName, SynchronizeShadowTaskDescriptor.ID, repo, age );
    }

}

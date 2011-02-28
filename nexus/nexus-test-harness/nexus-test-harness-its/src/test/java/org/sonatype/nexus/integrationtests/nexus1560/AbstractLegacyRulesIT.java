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
package org.sonatype.nexus.integrationtests.nexus1560;

import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public abstract class AbstractLegacyRulesIT
    extends AbstractPrivilegeTest
{

    protected static final String NEXUS1560_GROUP = "nexus1560-group";

    protected Gav gavArtifact1;

    protected Gav gavArtifact2;

    @BeforeMethod
    public void createGav1()
        throws Exception
    {
        this.gavArtifact1 =
            new Gav( "nexus1560", "artifact", "1.0", null, "jar", null, null, null, false, null, false, null );
        this.gavArtifact2 =
            new Gav( "nexus1560", "artifact", "2.0", null, "jar", null, null, null, false, null, false, null );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.runTask( "nexus1560-repo", RebuildMavenMetadataTaskDescriptor.ID, repo );
        ScheduledServicePropertyResource repo2 = new ScheduledServicePropertyResource();
        repo2.setKey( "repositoryId" );
        repo2.setValue(  REPO_TEST_HARNESS_REPO2 );
        TaskScheduleUtil.runTask( "nexus1560-repo2", RebuildMavenMetadataTaskDescriptor.ID, repo2 );
    }

    protected Response download( String downloadUrl )
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        return RequestFacade.doGetRequest( downloadUrl );
    }

    protected Response failDownload( String downloadUrl )
        throws IOException
    {
        Response response = download( downloadUrl );
        Status status = response.getStatus();
        Assert.assertTrue( status.isError(), "Unable to download artifact from repository: " + status );
        return response;
    }

    protected Response successDownload( String downloadUrl )
    throws IOException
    {
        Response response = download( downloadUrl );
        Status status = response.getStatus();
        Assert.assertTrue( status.isSuccess(), "Unable to download artifact from repository: " + status );
        return response;
    }

}
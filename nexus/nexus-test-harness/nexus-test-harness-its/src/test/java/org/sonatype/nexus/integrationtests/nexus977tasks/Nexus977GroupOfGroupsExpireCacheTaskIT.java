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
package org.sonatype.nexus.integrationtests.nexus977tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.text.StringContains;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.ExpireCacheTaskDescriptor;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsExpireCacheTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void expireCache()
        throws Exception
    {
        Gav gav = GavUtil.newGav( getTestId(), "project", "1.0" );
        failDownload( gav );

        File dest = new File( localStorageDir, "nexus977tasks/1/nexus977tasks/project/1.0/project-1.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );
        failDownload( gav );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "g4" );
        TaskScheduleUtil.runTask( "ExpireCacheTaskDescriptor-snapshot", ExpireCacheTaskDescriptor.ID, repo );
        
        downloadArtifactFromRepository( "g4", gav, "target/downloads/nexus977tasks" );
    }

    private void failDownload( Gav gav )
        throws IOException
    {
        try
        {
            downloadArtifactFromRepository( "g4", gav, "target/downloads/nexus977tasks" );
            Assert.fail( "snapshot removal should have deleted this" );
        }
        catch ( FileNotFoundException e )
        {
            MatcherAssert.assertThat( e.getMessage(), StringContains.containsString( "404" ) );
        }
    }
}

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
package org.sonatype.nexus.plugins.p2.repository.its.p2r01;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;

import org.sonatype.nexus.plugins.p2.repository.internal.tasks.P2MetadataGeneratorTaskDescriptor;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusP2GeneratorIT;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.annotations.Test;

public class P2R0102RunP2MetadataGeneratorTaskIT
    extends AbstractNexusP2GeneratorIT
{

    public P2R0102RunP2MetadataGeneratorTaskIT()
    {
        super( "p2r01" );
    }

    /**
     * Scan & build metadata on already deployed bundles.
     */
    @Test
    public void test()
        throws Exception
    {
        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        createP2MetadataGeneratorCapability();

        final ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( P2MetadataGeneratorTaskDescriptor.REPO_OR_GROUP_FIELD_ID );
        repo.setValue( getTestRepositoryId() );
        TaskScheduleUtil.runTask( P2MetadataGeneratorTaskDescriptor.ID, repo );
        TaskScheduleUtil.waitForAllTasksToStop();

        final File p2Artifacts = downloadP2ArtifactsFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Artifacts has been downloaded", p2Artifacts, is( notNullValue() ) );
        assertThat( "p2Artifacts exists", p2Artifacts.exists(), is( true ) );
        // TODO compare downloaded file with an expected one

        final File p2Content = downloadP2ContentFor( "org.ops4j.base", "ops4j-base-lang", "1.2.3" );
        assertThat( "p2Content has been downloaded", p2Content, is( notNullValue() ) );
        assertThat( "p2Content exists", p2Content.exists(), is( true ) );
        // TODO compare downloaded file with an expected one
    }

}

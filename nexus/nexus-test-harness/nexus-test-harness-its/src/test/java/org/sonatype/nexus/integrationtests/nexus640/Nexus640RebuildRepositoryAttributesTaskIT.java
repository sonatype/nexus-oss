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
package org.sonatype.nexus.integrationtests.nexus640;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the rebuild repository attributes task.
 */
public class Nexus640RebuildRepositoryAttributesTaskIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void rebuildAttributes()
        throws Exception
    {
        // String attributePath = "storage/"+REPO_TEST_HARNESS_REPO+"/.nexus/attributes/nexus640/artifact/1.0.0/";
        String attributePath = "proxy/attributes/"+REPO_TEST_HARNESS_REPO+"/nexus640/artifact/1.0.0/";

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );

        File jar = new File( nexusWorkDir, attributePath + "artifact-1.0.0.jar" );
        Assert.assertTrue( jar.exists(), "Attribute files should be generated after rebuild" );
        File pom = new File( nexusWorkDir, attributePath + "artifact-1.0.0.pom" );
        Assert.assertTrue( pom.exists(), "Attribute files should be generated after rebuild" );

    }

}

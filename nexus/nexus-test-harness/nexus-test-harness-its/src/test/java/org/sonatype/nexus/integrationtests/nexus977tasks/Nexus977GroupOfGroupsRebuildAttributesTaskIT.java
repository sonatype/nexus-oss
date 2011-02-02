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

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsRebuildAttributesTaskIT
    extends AbstractNexusProxyIntegrationTest
{

    @Test
    public void rebuild()
        throws Exception
    {
        // add some extra artifacts
        File dest = new File( nexusWorkDir, "storage/r1/nexus977tasks/project/1.0/project-1.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r2/nexus977tasks/project/2.0/project-2.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        dest = new File( nexusWorkDir, "storage/r3/nexus977tasks/project/3.0/project-3.0.jar" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( getTestFile( "project.jar" ), dest );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setKey( "repositoryId" );
        repo.setValue( "g4" );
        TaskScheduleUtil.runTask( RebuildAttributesTaskDescriptor.ID, repo );

        DirectoryScanner scan = new DirectoryScanner();
        scan.setBasedir( new File( nexusWorkDir, "storage" ) );
        scan.addDefaultExcludes();
        scan.setExcludes( new String[] { "**/.nexus/attributes/" } );
        scan.scan();
        String[] storageContent = scan.getIncludedFiles();

        scan = new DirectoryScanner();
        //scan.setBasedir( new File( nexusWorkDir, "storage" ) );
        scan.setBasedir( new File( nexusWorkDir, "proxy/attributes" ) );
        scan.addDefaultExcludes();
        // scan.setIncludes( new String[] { "**/.nexus/attributes/" } );
        scan.scan();
        String[] attributesContent = scan.getIncludedFiles();

        // the paths will differ, but length should be equal
        Assert.assertEquals( attributesContent.length, storageContent.length );
    }

}

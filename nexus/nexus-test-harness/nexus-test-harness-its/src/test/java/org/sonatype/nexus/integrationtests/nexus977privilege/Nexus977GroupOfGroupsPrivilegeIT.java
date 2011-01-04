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
package org.sonatype.nexus.integrationtests.nexus977privilege;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus977GroupOfGroupsPrivilegeIT
    extends AbstractPrivilegeTest
{

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        RepositoryMessageUtil.updateIndexes( "g4" );
    }

    @Test
    public void testReadAll()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        giveUserRole( TEST_USER_NAME, "repo-all-read" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "1.0.1" );

        File artifact = downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File originalFile = this.getTestResourceAsFile( "projects/p1/project.jar" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );
    }

    @Test
    public void testReadG4()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPriv( TEST_USER_NAME, "g4" + "-read-priv", TargetPrivilegeDescriptor.TYPE, "1", null, "g4", "read" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "0.8" );

        File artifact = downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );

        assertTrue( artifact.exists() );

        File originalFile = this.getTestResourceAsFile( "projects/p5/project.jar" );

        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifact ) );
    }

    @Test
    public void testNoAccess()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        Gav gav = GavUtil.newGav( this.getTestId(), "project", "2.1" );

        try
        {
            downloadArtifactFromGroup( "g4", gav, "./target/downloaded-jars" );
            Assert.fail();
        }
        catch ( FileNotFoundException e )
        {
            Assert.assertTrue( e.getMessage().contains( "403" ) );
        }

    }
}

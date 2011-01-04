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
package org.sonatype.nexus.integrationtests.nexus598;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class name search functionality.
 */
public class Nexus598ClassnameSearchIT
    extends AbstractNexusIntegrationTest
{
    public Nexus598ClassnameSearchIT()
    {
        // TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void searchDeployedArtifact()
        throws Exception
    {
        List<NexusArtifact> artifacts =
            getSearchMessageUtil().searchForClassname(
                "org.sonatype.nexus.test.classnamesearch.ClassnameSearchTestHelper" );
        Assert.assertFalse( artifacts.isEmpty(), "Nexus598 artifact was not found" );
    }

    @Test
    public void unqualifiedSearchDeployedArtifact()
        throws Exception
    {
        List<NexusArtifact> artifacts = getSearchMessageUtil().searchForClassname( "ClassnameSearchTestHelper" );
        Assert.assertFalse( artifacts.isEmpty(), "Nexus598 artifact was not found" );
    }

    @Test
    public void searchUnexistentClass()
        throws Exception
    {
        // This test is meaningless, since it does use tokens that appear in other class ("class", "nexus", "test"), so
        // Index
        // _will_ return it
        // Fixed by removing the two problematic token, but this still makes this test meaningless and very UNSTABLE
        // List<NexusArtifact> artifacts =
        // SearchMessageUtil.searchForClassname(
        // "I.hope.this.class.name.is.not.available.at.nexus.repo.for.test.issue.Nexus598" );

        List<NexusArtifact> artifacts = getSearchMessageUtil().searchForClassname( "I.hope.this.name.is.not.available" );
        Assert.assertTrue( artifacts.isEmpty(), "The search found something, but it shouldn't." );
    }

}

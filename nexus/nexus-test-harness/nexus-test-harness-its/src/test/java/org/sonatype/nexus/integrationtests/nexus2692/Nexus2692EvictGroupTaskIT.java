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
package org.sonatype.nexus.integrationtests.nexus2692;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2692EvictGroupTaskIT
    extends AbstractEvictTaskIt
{

    @Test
    public void testEvictPublicGroup()
        throws Exception
    {
        int days = 6;
        // run Task
        runTask( days, "public" );

        // check files
        SortedSet<String> resultStorageFiles = getItemFilePaths();
        SortedSet<String> resultAttributeFiles = getAttributeFilePaths();

        // list of repos NOT in the public group
        List<String> nonPublicGroupMembers = new ArrayList<String>();
        nonPublicGroupMembers.add( "apache-snapshots" );

        SortedSet<String> expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );

        // calc the diff ( files that were deleted and should not have been )
        expectedResults.removeAll( resultStorageFiles );
        Assert.assertTrue( expectedResults.isEmpty(), "The following files where deleted and should not have been: "
            + prettyList( expectedResults ) );

        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        expectedResults.removeAll( resultAttributeFiles );
        Assert.assertTrue( expectedResults.isEmpty(),
            "The following attribute files where deleted and should not have been: " + prettyList( expectedResults ) );

        // now the other way
        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        resultStorageFiles.removeAll( expectedResults );
        Assert.assertTrue( resultStorageFiles.isEmpty(), "The following files should have been deleted: "
            + prettyList( resultStorageFiles ) );

        expectedResults = buildListOfExpectedFiles( days, nonPublicGroupMembers );
        resultAttributeFiles.removeAll( expectedResults );
        Assert.assertTrue( resultAttributeFiles.isEmpty(), "The following files should have been deleted: "
            + prettyList( resultAttributeFiles ) );

        // make sure we don't have any empty directories
        checkForEmptyDirectories();
    }
}

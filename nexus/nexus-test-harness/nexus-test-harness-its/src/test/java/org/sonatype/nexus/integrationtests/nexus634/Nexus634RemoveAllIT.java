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
package org.sonatype.nexus.integrationtests.nexus634;

import java.io.File;
import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test SnapshotRemoverTask to remove all artifacts
 * 
 * @author marvin
 */
public class Nexus634RemoveAllIT
    extends AbstractSnapshotRemoverIT
{

    @Test
    public void removeAllSnapshots()
        throws Exception
    {
        // This is THE important part
        runSnapshotRemover( "nexus-test-harness-snapshot-repo", 0, 0, true );

        /*
         * This IT is now very wrong, as snapshot remover will no longer remove -SNAPSHOT artifacts,
         * only timestamped snapshot artifacts (unless there is a release version and remove when released is set)
        // this IT is wrong: nexus will remove the parent folder too, if the GAV folder is emptied completely
        // Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        // Assert.assertTrue( "All artifacts should be deleted by SnapshotRemoverTask. Found: " + jars, jars.isEmpty()
        // );

        // looking at the IT resources, there is only one artifact in there, hence, the dir should be removed
        Assert.assertFalse(
            "The folder should be removed since all artifacts should be gone, instead there are files left!",
            artifactFolder.exists() );
        */
        
        Collection<File> jars = listFiles( artifactFolder, new String[] { "jar" }, false );
        Assert.assertEquals( 1, jars.size() );
    }

}

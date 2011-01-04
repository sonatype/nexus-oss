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
package org.sonatype.nexus.index;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.proxy.repository.GroupRepository;

public class GroupReindexIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testGroupReindex()
        throws Exception
    {
        fillInRepo();

        GroupRepository group = (GroupRepository) repositoryRegistry.getRepository( "public" );

        File groupRoot = new File( new URL( group.getLocalUrl() ).toURI() );
        File index = new File( groupRoot, ".index" );

        File indexFile = new File( index, "nexus-maven-repository-index.gz" );
        File incrementalIndexFile = new File( index, "nexus-maven-repository-index.1.gz" );

        assertFalse( "No index .gz file should exist.", indexFile.exists() );
        assertFalse( "No incremental chunk should exists.", incrementalIndexFile.exists() );

        indexerManager.reindexRepository( null, group.getId(), true );

        assertTrue( "Index .gz file should exist.", indexFile.exists() );
        assertFalse( "No incremental chunk should exists.", incrementalIndexFile.exists() );

        // copy some _new_ stuff, not found in any of the members
        File sourceApacheSnapshotsRoot = new File( getBasedir(), "src/test/resources/reposes/apache-snapshots-2" );
        File snapshotsRoot = new File( new URL( snapshots.getLocalUrl() ).toURI() );
        copyDirectory( sourceApacheSnapshotsRoot, snapshotsRoot );
        indexerManager.reindexRepository( null, group.getId(), false );

        assertTrue( "Index .gz file should exist.", indexFile.exists() );
        assertTrue( "Incremental chunk should exists.", incrementalIndexFile.exists() );

        assertTrue( "We expected less than 300 bytes but got " + incrementalIndexFile.length(),
            incrementalIndexFile.length() < 300 );

    }
}

/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import org.junit.Test;

public class GroupUpdateIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    @Test
    public void testGroupUpdate()
        throws Exception
    {
        assertTrue( true );

        // removed as functionallity has been removed for now
        /*
         * fillInRepo(); indexerManager.reindexAllRepositories( null, true ); searchFor( "org.sonatype.plexus", 1,
         * "snapshots" ); searchFor( "org.sonatype.plexus", 1, "public" ); searchFor( "org.sonatype.test-evict", 1,
         * "apache-snapshots" ); searchFor( "org.sonatype.test-evict", 0, "public" ); GroupRepository group =
         * (GroupRepository) repositoryRegistry.getRepository( "public" ); group.removeMemberRepositoryId(
         * snapshots.getId() ); super.nexusConfiguration.saveConfiguration(); waitForTasksToStop(); group =
         * (GroupRepository) repositoryRegistry.getRepository( "public" ); assertFalse(
         * group.getMemberRepositoryIds().contains( snapshots.getId() ) ); searchFor( "org.sonatype.plexus", 0, "public"
         * ); group.addMemberRepositoryId( apacheSnapshots.getId() ); super.nexusConfiguration.saveConfiguration();
         * waitForTasksToStop(); searchFor( "org.sonatype.test-evict", 1, "public" );
         */
    }
}

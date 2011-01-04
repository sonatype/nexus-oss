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

public class GroupUpdateIndexerManagerTest
    extends AbstractIndexerManagerTest
{

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

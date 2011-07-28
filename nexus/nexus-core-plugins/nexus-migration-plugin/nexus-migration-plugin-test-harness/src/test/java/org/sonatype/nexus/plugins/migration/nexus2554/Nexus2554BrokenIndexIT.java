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
package org.sonatype.nexus.plugins.migration.nexus2554;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus2554BrokenIndexIT
    extends AbstractMigrationIntegrationTest
{

    @Test
    public void brokenIndex()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "20090825.081708.zip" ) );
        commitMigration( migrationSummary );

        checkRepository( "ext-snapshots-local" );
        checkRepository( "libs-releases-local" );
        checkRepository( "libs-snapshots-local" );
        checkRepository( "plugins-releases-local" );
        checkRepository( "plugins-snapshots-local" );
        checkRepository( "ext-releases-local" );
        checkRepository( "repo1" );
        checkSnapshotReleaseRepository( "java.net.m2" );
        checkSnapshotReleaseRepository( "java.net.m1" );
        checkRepository( "jboss" );
        checkRepository( "codehaus" );
        checkSnapshotReleaseRepository( "sss" );
        checkSnapshotReleaseRepository( "abc" );
        checkSnapshotReleaseRepository( "xyz" );

        checkGroup( "remote-repos" );
        checkGroup( "libs-releases" );
        checkGroup( "plugins-releases" );
        checkGroup( "libs-snapshots" );
        checkGroup( "plugins-snapshots" );
        checkGroup( "vvv" );

        String logs = FileUtils.fileRead( getNexusLogFile() );
        Assert.assertFalse(
                            logs,
                            logs.contains( "Error message is: java.lang.NullPointerException Strack trace: java.lang.NullPointerException" ) );
        Assert.assertFalse( logs, logs.contains( "RepositoryNotAvailableException" ) );
        Assert.assertFalse( logs, logs.contains( "java.lang.NullPointerException" ) );
    }

}

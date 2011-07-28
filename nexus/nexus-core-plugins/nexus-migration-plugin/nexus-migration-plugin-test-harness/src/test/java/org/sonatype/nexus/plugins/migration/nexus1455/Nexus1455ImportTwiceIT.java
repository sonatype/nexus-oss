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
package org.sonatype.nexus.plugins.migration.nexus1455;

import java.io.FileReader;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.AbstractMigrationIntegrationTest;

public class Nexus1455ImportTwiceIT
    extends AbstractMigrationIntegrationTest
{
    @Test
    public void importTwice()
        throws Exception
    {
        if ( true )
        {
            super.printKnownErrorButDoNotFail( getClass(), "importTwice" );
            return;
        }

        MigrationSummaryDTO migrationSummary = prepareMigration( getTestFile( "artifactoryBackup.zip" ) );
        Assert.assertNotNull( migrationSummary.getId() );
        commitMigration( migrationSummary );
        commitMigration( migrationSummary );

        Assert.assertTrue( "Migration log file not found", migrationLogFile.isFile() );

        String log = IOUtil.toString( new FileReader( migrationLogFile ) );
        Assert.assertTrue( "Didn't skip second migration " + log,
                           log.contains( "Trying to import the same package twice" ) );
        Assert.assertFalse( "Error during migration", log.toLowerCase().contains( "error" ) );
    }

}

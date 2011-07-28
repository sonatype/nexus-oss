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
package org.sonatype.nexus.plugins.migration;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;

public abstract class AbstractMigrationPrivilegeTest
    extends AbstractPrivilegeTest
{

    @BeforeClass
    public static void enableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
    
    protected final String ARTIFACTORY_MIGRATOR = "artifactory-migrate";

    abstract protected File getBackupFile();

    protected Status doMigration()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = ImportMessageUtil.importBackup( getBackupFile() );

        return ImportMessageUtil.commitImport( migrationSummary ).getStatus();
    }
    
    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();
        copyConfigFile( "logback-migration.xml", getTestProperties(), WORK_CONF_DIR );
    }

}

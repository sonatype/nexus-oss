/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4635;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.StatusResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 1st start of "virgin" Nexus<BR>
 * https://issues.sonatype.org/browse/NEXUS-4635
 * 
 * <pre>
 * <firstStart>true</firstStart>
 * <instanceUpgraded>false</instanceUpgraded>
 * <configurationUpgraded>false</configurationUpgraded>
 * </pre>
 */
public class Nexus4635FirstStartIT
    extends AbstractNexusIntegrationTest
{
    @BeforeClass
    protected void disableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        // after we copied, we
        // selectively delete config files to make nexus believe this is 1st start, but leave stuff like logback config
        // in place!
        // FileUtils.forceDelete fits nice, since it will throw IOEx if unable to delete (will not be dumb like
        // File.delete() is)
        final File nexusXml = new File( new File( WORK_CONF_DIR ), "nexus.xml" );
        final File securityXml = new File( new File( WORK_CONF_DIR ), "security.xml" );
        final File securityConfigurationXml = new File( new File( WORK_CONF_DIR ), "security-configuration.xml" );

        FileUtils.forceDelete( nexusXml );
        FileUtils.forceDelete( securityXml );
        FileUtils.forceDelete( securityConfigurationXml );
    }

    @Test
    public void checkState()
        throws Exception
    {
        StatusResource status = getNexusStatusUtil().getNexusStatus().getData();
        assertTrue( status.isFirstStart() );
        assertFalse( status.isInstanceUpgraded() );
        assertFalse( status.isConfigurationUpgraded() );
    }

}

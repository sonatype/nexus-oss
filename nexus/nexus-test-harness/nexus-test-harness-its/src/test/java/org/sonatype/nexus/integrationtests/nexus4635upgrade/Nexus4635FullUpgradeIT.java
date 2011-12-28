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
package org.sonatype.nexus.integrationtests.nexus4635upgrade;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.StatusResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * placing old config to force upgrade and firing up Nexus.<BR>
 * https://issues.sonatype.org/browse/NEXUS-4635
 * 
 * <pre>
 * <firstStart>false</firstStart>
 * <instanceUpgraded>true</instanceUpgraded>
 * <configurationUpgraded>true</configurationUpgraded>
 * </pre>
 */
public class Nexus4635FullUpgradeIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public void doNotVerifyConfig()
    {
        // no verification, since it would upgrade and hence, modify it
        setVerifyNexusConfigBeforeStart( false );
    }

    @Test
    public void checkState()
        throws Exception
    {
        StatusResource status = getNexusStatusUtil().getNexusStatus().getData();
        assertFalse( status.isFirstStart() );
        assertTrue( status.isInstanceUpgraded() );
        assertTrue( status.isConfigurationUpgraded() );
    }
}

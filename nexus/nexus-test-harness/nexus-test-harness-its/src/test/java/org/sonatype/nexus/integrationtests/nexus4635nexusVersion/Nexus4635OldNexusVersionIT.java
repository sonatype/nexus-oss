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
package org.sonatype.nexus.integrationtests.nexus4635nexusVersion;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.StatusResource;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.testng.annotations.Test;

/**
 * tampering the nexusVersion to 1.9.3<BR>
 * https://issues.sonatype.org/browse/NEXUS-4635
 * 
 * <pre>
 * <firstStart>false</firstStart>
 * <instanceUpgraded>true</instanceUpgraded>
 * <configurationUpgraded>false</configurationUpgraded>
 * </pre>
 */
public class Nexus4635OldNexusVersionIT
    extends AbstractNexusIntegrationTest
{

    public NexusConfigUtil getNexusConfigUtil()
    {
        // prevent IT from creating nexus.xml
        return new NexusConfigUtil( this )
        {
            @Override
            public void validateConfig()
                throws Exception
            {
                super.validateConfig();

                NexusConfiguration config = getTest().getITPlexusContainer().lookup( NexusConfiguration.class );
                config.loadConfiguration( true );
                config.getConfigurationModel().setNexusVersion( "1.9.3" );
                config.saveConfiguration();
            }
        };
    }

    @Test
    public void checkState()
        throws Exception
    {
        StatusResource status = getNexusStatusUtil().getNexusStatus().getData();
        assertFalse( status.isFirstStart() );
        assertTrue( status.isInstanceUpgraded() );
        assertFalse( status.isConfigurationUpgraded() );
    }
}

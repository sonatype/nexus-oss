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
package org.sonatype.nexus.integrationtests.nexus4635upgrade;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
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
    protected void disableSecurity()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );
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

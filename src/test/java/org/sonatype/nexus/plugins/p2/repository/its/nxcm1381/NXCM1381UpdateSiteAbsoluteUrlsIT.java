/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1381;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;


public class NXCM1381UpdateSiteAbsoluteUrlsIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM1381UpdateSiteAbsoluteUrlsIT()
    {
        super( "updatesiteproxy" );
    }

    @Test
    public void testSiteWithAbsoluteUrls()
        throws Exception
    {
        File nexusDir = new File( nexusWorkDir, "storage/updatesiteproxy" );

        TaskScheduleUtil.run( "1" );
        TaskScheduleUtil.waitForAllTasksToStop();

        Assert.assertTrue( new File( nexusDir, "features/org.sonatype.nexus.plugins.p2.repository.its.feature_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "plugins/org.sonatype.nexus.plugins.p2.repository.its.bundle_1.0.0.jar" ).exists() );
    }
}

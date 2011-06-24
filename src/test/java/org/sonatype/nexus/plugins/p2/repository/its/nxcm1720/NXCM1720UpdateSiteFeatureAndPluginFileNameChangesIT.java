/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1720;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class NXCM1720UpdateSiteFeatureAndPluginFileNameChangesIT
    extends AbstractNexusProxyP2IT
{
    public NXCM1720UpdateSiteFeatureAndPluginFileNameChangesIT()
    {
        super( "nxcm1720" );
    }

    @Test
    public void test()
        throws Exception
    {
        final File nexusDir = new File( nexusWorkDir, "storage/nxcm1720" );

        TaskScheduleUtil.run( "1" );
        TaskScheduleUtil.waitForAllTasksToStop();

        Assert.assertFalse( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0-feature.jar" ).exists() );
        Assert.assertFalse( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature.local_1.0.0-feature.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature2_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature.local_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "features/com.sonatype.nexus.p2.its.feature2.local_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" ).exists() );
        Assert.assertTrue( new File( nexusDir, "plugins/com.sonatype.nexus.p2.its.bundle.local_1.0.0.jar" ).exists() );
    }
}

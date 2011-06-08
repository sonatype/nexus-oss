/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm0581;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;


public class NXCM581UpdateSiteProxyIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM581UpdateSiteProxyIT()
    {
        super( "updatesiteproxy" );
    }

    @Test
    public void updatesiteproxy() throws Exception {
        String nexusTestRepoUrl = getNexusTestRepoUrl();

        File installDir = new File("target/eclipse/nxcm0072");

        TaskScheduleUtil.run( "1" );
        TaskScheduleUtil.waitForAllTasksToStop();

        installUsingP2(
            nexusTestRepoUrl,
            "org.sonatype.nexus.plugins.p2.repository.its.feature.feature.group",
            installDir.getCanonicalPath() );

        File feature = new File(installDir, "features/org.sonatype.nexus.plugins.p2.repository.its.feature_1.0.0");
        Assert.assertTrue(feature.exists() && feature.isDirectory());

        File bundle = new File(installDir, "plugins/org.sonatype.nexus.plugins.p2.repository.its.bundle_1.0.0.jar");
        Assert.assertTrue(bundle.canRead());
    }
}

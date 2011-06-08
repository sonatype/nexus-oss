/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm0072;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;


public class NXCM72P2ProxyIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM72P2ProxyIT()
    {
        super( "p2proxy" );
    }

    @Test
    public void p2repository() throws Exception {
        String nexusTestRepoUrl = getNexusTestRepoUrl();

        File installDir = new File("target/eclipse/nxcm0072");

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

/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.meclipse0393;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.TestProperties;


public class MECLIPSE393P2ProxyCompositeWithMirrorIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public MECLIPSE393P2ProxyCompositeWithMirrorIT()
    {
        super( "p2proxycompositewithmirror" );
    }

    @Override
    public void copyTestResources()
        throws IOException
    {
        super.copyTestResources();
        String proxyRepoBaseUrl = TestProperties.getString( "proxy.repo.base.url" );

        replaceInFile( localStorageDir + "/p2repocompositewithmirror/memberrepo1/artifacts.xml",
                       "${proxy-repo-base-url}", proxyRepoBaseUrl );
        replaceInFile( localStorageDir + "/p2repocompositewithmirror/memberrepo2/artifacts.xml",
                       "${proxy-repo-base-url}", proxyRepoBaseUrl );

        replaceInFile( localStorageDir + "/p2repocompositewithmirror/memberrepo1/mirrors.xml",
                       "${proxy-repo-base-url}", proxyRepoBaseUrl );
        replaceInFile( localStorageDir + "/p2repocompositewithmirror/memberrepo2/mirrors.xml",
                       "${proxy-repo-base-url}", proxyRepoBaseUrl );
    }

    @Test
    public void testProxyWithMirror()
        throws Exception
    {
        String nexusTestRepoUrl = getNexusTestRepoUrl();

        File installDir = new File( "target/eclipse/meclipse0393" );

        installUsingP2( nexusTestRepoUrl, "org.sonatype.nexus.plugins.p2.repository.its.feature.feature.group",
                        installDir.getCanonicalPath() );

        File feature = new File( installDir, "features/org.sonatype.nexus.plugins.p2.repository.its.feature_1.0.0" );
        Assert.assertTrue( feature.exists() && feature.isDirectory() );

        File bundle = new File( installDir, "plugins/org.sonatype.nexus.plugins.p2.repository.its.bundle_1.0.0.jar" );
        Assert.assertTrue( bundle.canRead() );
    }
}

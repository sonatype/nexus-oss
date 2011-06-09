/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1941;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IntegrationIT;
import org.sonatype.nexus.test.utils.TestProperties;

public class NXCM1941P2ProxyWithFTPMirrorIT
    extends AbstractNexusProxyP2IntegrationIT
{
    public NXCM1941P2ProxyWithFTPMirrorIT()
    {
        super( "nxcm1941" );
    }

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        final String proxyRepoBaseUrl = TestProperties.getString( "proxy.repo.base.url" );
        Assert.assertTrue( proxyRepoBaseUrl.startsWith( "http://" ) );

        replaceInFile( "target/nexus/proxy-repo/p2repowithftpmirror/artifacts.xml", "${proxy-repo-base-url}",
            proxyRepoBaseUrl );
        replaceInFile( "target/nexus/proxy-repo/p2repowithftpmirror/mirrors.xml", "${proxy-repo-base-url}",
            proxyRepoBaseUrl );
        replaceInFile( "target/nexus/proxy-repo/p2repowithftpmirror/mirrors.xml", "${ftp-proxy-repo-base-url}", "ftp"
            + proxyRepoBaseUrl.substring( 4 ) );
    }

    @Test
    public void test()
        throws Exception
    {
        final String nexusTestRepoUrl = getNexusTestRepoUrl();

        final File installDir = new File( "target/eclipse/nxcm1941" );

        installUsingP2( nexusTestRepoUrl, "com.sonatype.nexus.p2.its.feature.feature.group",
            installDir.getCanonicalPath() );

        final File feature = new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertTrue( feature.exists() && feature.isDirectory() );

        final File bundle = new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertTrue( bundle.canRead() );
    }
}

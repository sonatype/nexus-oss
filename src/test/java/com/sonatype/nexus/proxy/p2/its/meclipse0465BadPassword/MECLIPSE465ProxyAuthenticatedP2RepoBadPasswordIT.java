/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.proxy.p2.its.meclipse0465BadPassword;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.sonatype.nexus.proxy.p2.its.AbstractNexusProxyP2SecureIntegrationIT;
import com.sonatype.nexus.proxy.p2.its.P2ITException;

public class MECLIPSE465ProxyAuthenticatedP2RepoBadPasswordIT
    extends AbstractNexusProxyP2SecureIntegrationIT
{
    public MECLIPSE465ProxyAuthenticatedP2RepoBadPasswordIT()
    {
        super( "proxyAuthenticatedP2RepoBadPassword" );
    }

    @Test
    public void MECLIPSE465ProxyAuthenticatedP2RepoBadPassword()
        throws Exception
    {
        String nexusTestRepoUrl = getNexusTestRepoUrl();

        File installDir = new File( "target/eclipse/meclipse0465BadPassword" );

        try
        {
            installUsingP2( nexusTestRepoUrl, "com.sonatype.nexus.p2.its.feature.feature.group", installDir.getCanonicalPath() );
            Assert.fail( "Expected P2ITException" );
        }
        catch ( P2ITException e )
        {
            if ( !e.getMessage().contains(
                                           "No repository found at " + getBaseNexusUrl()
                                               + "content/repositories/proxyAuthenticatedP2RepoBadPassword/" ) )
            {
                throw e;
            }
        }

        File feature = new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertFalse( feature.exists() );

        File bundle = new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertFalse( bundle.canRead() );
    }
}

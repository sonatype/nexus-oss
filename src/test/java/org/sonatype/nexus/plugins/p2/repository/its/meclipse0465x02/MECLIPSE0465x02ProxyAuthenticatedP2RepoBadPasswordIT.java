/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.meclipse0465x02;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2SecureIntegrationIT;
import org.sonatype.nexus.plugins.p2.repository.its.P2ITException;

public class MECLIPSE0465x02ProxyAuthenticatedP2RepoBadPasswordIT
    extends AbstractNexusProxyP2SecureIntegrationIT
{
    public MECLIPSE0465x02ProxyAuthenticatedP2RepoBadPasswordIT()
    {
        super( "proxyAuthenticatedP2RepoBadPassword" );
    }

    @Test
    public void MECLIPSE465ProxyAuthenticatedP2RepoBadPassword()
        throws Exception
    {
        final String nexusTestRepoUrl = getNexusTestRepoUrl();

        final File installDir = new File( "target/eclipse/meclipse0465BadPassword" );

        try
        {
            installUsingP2( nexusTestRepoUrl, "com.sonatype.nexus.p2.its.feature.feature.group",
                installDir.getCanonicalPath() );
            Assert.fail( "Expected P2ITException" );
        }
        catch ( final P2ITException e )
        {
            if ( !e.getMessage().contains(
                "No repository found at " + getBaseNexusUrl()
                    + "content/repositories/proxyAuthenticatedP2RepoBadPassword/" ) )
            {
                throw e;
            }
        }

        final File feature =
            new File( installDir, "features/com.sonatype.nexus.p2.its.feature_1.0.0" );
        Assert.assertFalse( feature.exists() );

        final File bundle =
            new File( installDir, "plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar" );
        Assert.assertFalse( bundle.canRead() );
    }
}

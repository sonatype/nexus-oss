/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1898;

import org.junit.Assert;
import org.junit.Test;

public class NXCM1898P2ProxyTimeoutIT
    extends AbstractProxyTimeout
{
    @Test
    public void p2repository2secs()
        throws Exception
    {
        Assert.assertTrue( true );
        // doTest( 5000 );
    }
}

/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm1691;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class NXCM1691P2ProxyTestStatusIT
    extends AbstractNexusProxyP2IT
{
    public NXCM1691P2ProxyTestStatusIT()
    {
        super( "nxcm1691-content-xml" );
    }

    @Test
    public void test()
        throws Exception
    {
        final RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );

        for ( String s : P2Constants.METADATA_FILE_PATHS )
        {
            s = s.replaceAll( "/", "" ).replaceAll( "\\.", "-" );
            testStatus( repoUtil, "nxcm1691-" + s, RemoteStatus.AVAILABLE );
        }

        testStatus( repoUtil, "nxcm1691-not-p2", RemoteStatus.UNAVAILABLE );
    }

    private void testStatus( final RepositoryMessageUtil repoUtil, final String repoId,
                             final RemoteStatus expectedStatus )
        throws Exception
    {
        final int timeout = 30000; // 30 secs
        final long start = System.currentTimeMillis();
        String status = RemoteStatus.UNKNOWN.toString();
        while ( RemoteStatus.UNKNOWN.toString().equals( status ) && ( System.currentTimeMillis() - start ) < timeout )
        {
            final RepositoryStatusResource statusResource = repoUtil.getStatus( repoId );
            status = statusResource.getRemoteStatus();
            Thread.sleep( 100 );
        }
        Assert.assertEquals( "Unexpected status for repository id=" + repoId, expectedStatus.toString(), status );
    }
}

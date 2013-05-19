/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1329;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.rest.model.MirrorStatusResource;
import org.sonatype.nexus.rest.model.MirrorStatusResourceListResponse;

public class Nexus1329UnavailableIT
    extends AbstractMirrorIT
{

    /**
     * 5. download from mirror fails. download from repository fails. the mirror is NOT blacklisted
     */
    @Test
    public void downloadUnavailable()
        throws Exception
    {
        List<String> repoUrls = server.addServer( "repository", 500 );
        List<String> mirror1Urls = server.addServer( "mirror1", 500 );
        server.addServer( "mirror2", 500 );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, null, false, null );

        try
        {
            this.downloadArtifactFromRepository( REPO, gav, "./target/downloads/nexus1329" );
            Assert.fail( "Artifact is not available, shouldn't download!" );
        }
        catch ( FileNotFoundException e )
        {
            // expected
        }

        Assert.assertFalse( "Nexus should try repository canonical url " + repoUrls, repoUrls.isEmpty() );
        Assert.assertFalse( "Nexus should try mirror 1 " + mirror1Urls, mirror1Urls.isEmpty() );

        MirrorStatusResourceListResponse response = this.messageUtil.getMirrorsStatus( REPO );

        MirrorStatusResource one = (MirrorStatusResource) response.getData().get( 0 );

        Assert.assertEquals( one.getUrl(), "http://localhost:" + webProxyPort + "/mirror1" );
        Assert.assertEquals( one.getStatus(), "Available" );
    }

}

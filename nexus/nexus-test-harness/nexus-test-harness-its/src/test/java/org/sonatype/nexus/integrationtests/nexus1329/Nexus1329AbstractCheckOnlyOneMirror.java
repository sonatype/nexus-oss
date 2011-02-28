/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1329;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public abstract class Nexus1329AbstractCheckOnlyOneMirror
    extends AbstractMirrorIT
{
    /**
     * Nexus should try only the first mirror server
     */
    @Test
    public void checkSingleMirror()
        throws Exception
    {
        beforeCheck();
        
        File content = getTestFile( "basic" );

        server.addServer( "repository", content );
        List<String> mirror1Urls = server.addServer( "mirror1", HttpServletResponse.SC_NOT_FOUND );
        List<String> mirror2Urls = server.addServer( "mirror2", HttpServletResponse.SC_NOT_FOUND );

        server.start();

        Gav gav =
            new Gav( "nexus1329", "sample", "1.0.0", null, "xml", null, null, null, false, null, false, null );

        File artifactFile = this.downloadArtifactFromRepository( REPO, gav, "./target/downloads/nexus1329" );

        File originalFile = this.getTestFile( "basic/nexus1329/sample/1.0.0/sample-1.0.0.xml" );
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( originalFile, artifactFile ) );

        Assert.assertTrue( mirror1Urls.size() > 0, "Nexus should access first mirror " + mirror1Urls );
        Assert.assertTrue( mirror2Urls.isEmpty(), "Nexus should not access second mirror " + mirror2Urls );
    }
    
    protected void beforeCheck()
        throws Exception
    {
    }
}

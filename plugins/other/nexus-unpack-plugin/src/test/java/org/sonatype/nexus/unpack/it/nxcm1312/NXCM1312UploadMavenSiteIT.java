/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.unpack.it.nxcm1312;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TestProperties;

import org.sonatype.nexus.unpack.it.AbstractUnpackIT;

public class NXCM1312UploadMavenSiteIT
    extends AbstractUnpackIT
{

    @Test
    public void build()
        throws Exception
    {
        System.setProperty( "maven.home", TestProperties.getString( "maven-basedir" ) );

        Verifier verifier = new Verifier( getTestFile( "upload-unpack-test" ).getAbsolutePath(), false );

        String logname = "logs/maven-deploy/nxcm1312/maven.log";
        new File( verifier.getBasedir(), logname ).getParentFile().mkdirs();
        verifier.setLogFileName( logname );

        verifier.setLocalRepo( TestProperties.getFile( "maven-repository" ).getAbsolutePath() );

        verifier.resetStreams();

        // verifier.setCliOptions( options );
        verifier.executeGoal( "install" );

        File root = new File( nexusWorkDir, "storage/nexus-test-harness-repo2/some/path" );
        Assert.assertTrue(root.exists());
        Assert.assertTrue( new File( root, "b.bin" ).exists() );
        Assert.assertTrue( new File( root, "x/a.txt" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/MANIFEST.MF" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/maven/org.sonatype.nexus.unpack/upload-unpack-test/pom.properties" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/maven/org.sonatype.nexus.unpack/upload-unpack-test/pom.xml" ).exists() );
    }

}

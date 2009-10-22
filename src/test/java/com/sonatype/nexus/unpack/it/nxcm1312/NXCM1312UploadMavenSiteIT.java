package com.sonatype.nexus.unpack.it.nxcm1312;

import java.io.File;

import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.utils.TestProperties;

import com.sonatype.nexus.unpack.it.AbstractUnpackIT;

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
        Assert.assertTrue( root.exists() );
        Assert.assertTrue( new File( root, "b.bin" ).exists() );
        Assert.assertTrue( new File( root, "x/a.txt" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/MANIFEST.MF" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/maven/com.sonatype.tests/upload-unpack-test/pom.properties" ).exists() );
        Assert.assertTrue( new File( root, "META-INF/maven/com.sonatype.tests/upload-unpack-test/pom.xml" ).exists() );
    }

}

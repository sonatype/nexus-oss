package org.sonatype.nexus.proxy.maven;

import junit.framework.Assert;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;

public class ArtifactStoreRequestTest
    extends AbstractProxyTestEnvironment
{

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        return new M2TestsuiteEnvironmentBuilder( ss );
    }
    
    public void testNoDots() throws Exception
    {
        Gav gav = new Gav("nodots", "artifact", "1.0", null, "xml", null, null, null, false, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true );
        
        Assert.assertEquals( "/nodots/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    public void testDots() throws Exception
    {
        Gav gav = new Gav("a.bunch.of.dots.yeah", "artifact", "1.0", null, "xml", null, null, null, false, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true );
        
        Assert.assertEquals( "/a/bunch/of/dots/yeah/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    // undefined extra dot
//    public void testExtraDot() throws Exception
//    {
//        Gav gav = new Gav("extra..dot", "artifact", "1.0", null, "xml", null, null, null, false, false, null, false, null);
//        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
//        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true );
//        
//        Assert.assertEquals( "/extra/dot/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
//    }
    
    public void testGroupStartsWithDot() throws Exception
    {
        Gav gav = new Gav(".meta/foo/bar", "artifact", "1.0", null, "xml", null, null, null, false, false, null, false, null);
        MavenRepository mavenRepository = (MavenRepository) this.getRepositoryRegistry().getRepository( "repo1" );
        ArtifactStoreRequest request = new ArtifactStoreRequest( mavenRepository, gav, true );
        
        Assert.assertEquals( "/.meta/foo/bar/artifact/1.0/artifact-1.0.xml", request.getRequestPath() );
    }
    
    

}

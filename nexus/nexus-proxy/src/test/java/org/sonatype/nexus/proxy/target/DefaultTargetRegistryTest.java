package org.sonatype.nexus.proxy.target;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Arrays;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

public class DefaultTargetRegistryTest
    extends PlexusTestCase
{
    protected TargetRegistry targetRegistry;

    protected ContentClass maven1;

    protected ContentClass maven2;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        maven1 = new Maven1ContentClass();

        maven2 = new Maven2ContentClass();

        targetRegistry = lookup( TargetRegistry.class );

        // adding two targets
        Target t1 = new Target( "maven2-public", "Maven2 (public)", maven2, Arrays
            .asList( new String[] { "/org/apache/maven/((?!sources\\.).)*" } ) );

        targetRegistry.addRepositoryTarget( t1 );

        Target t2 = new Target( "maven2-with-sources", "Maven2 sources", maven2, Arrays
            .asList( new String[] { "/org/apache/maven/.*" } ) );

        targetRegistry.addRepositoryTarget( t2 );

        Target t3 = new Target( "maven1", "Maven1", maven1, Arrays.asList( new String[] { "/org\\.apache\\.maven.*" } ) );

        targetRegistry.addRepositoryTarget( t3 );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testSimpleM2()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven2 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9.pom" );

        assertNotNull( ts );

        assertEquals( 2, ts.getMatches().size() );

        assertEquals( 1, ts.getMatchedRepositoryIds().size() );

        assertEquals( "dummy", ts.getMatchedRepositoryIds().iterator().next() );

        TargetSet ts1 = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertNotNull( ts1 );

        assertEquals( 1, ts1.getMatches().size() );

        assertEquals( "maven2-with-sources", ts1.getMatches().iterator().next().getTarget().getId() );

        // adding them
        ts.addTargetSet( ts1 );

        assertEquals( 2, ts.getMatches().size() );

        assertEquals( 1, ts.getMatchedRepositoryIds().size() );
    }

    public void testSimpleM1()
    {
        // create a dummy
        Repository repository = createMock( Repository.class );
        expect( repository.getRepositoryContentClass() ).andReturn( maven1 ).anyTimes();
        expect( repository.getId() ).andReturn( "dummy" ).anyTimes();

        replay( repository );

        TargetSet ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org.apache.maven/jars/maven-model-v3-2.0.jar" );

        assertNotNull( ts );

        assertEquals( 1, ts.getMatches().size() );

        ts = targetRegistry.getTargetsForRepositoryPath(
            repository,
            "/org/apache/maven/maven-core/2.0.9/maven-core-2.0.9-sources.jar" );

        assertNotNull( ts );

        assertEquals( 0, ts.getMatches().size() );
    }

}

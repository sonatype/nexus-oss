package org.sonatype.nexus.proxy.target;

import java.util.Arrays;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Support class for DefaultTargetRegistry testing
 * 
 * @author Marvin Froeder ( velo at sonatype.com )
 */
public abstract class AbstractDefaultTargetRegistryTest
    extends AbstractNexusTestCase
{

    protected ApplicationConfiguration applicationConfiguration;

    protected TargetRegistry targetRegistry;

    protected ContentClass maven1;

    protected ContentClass maven2;

    public AbstractDefaultTargetRegistryTest()
    {
        super();
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        applicationConfiguration = lookup( ApplicationConfiguration.class );

        maven1 = new Maven1ContentClass();

        maven2 = new Maven2ContentClass();

        targetRegistry = lookup( TargetRegistry.class );

        // adding two targets
        Target t1 =
            new Target( "maven2-public", "Maven2 (public)", maven2,
                Arrays.asList( new String[] { "/org/apache/maven/((?!sources\\.).)*" } ) );

        targetRegistry.addRepositoryTarget( t1 );

        Target t2 =
            new Target( "maven2-with-sources", "Maven2 sources", maven2,
                Arrays.asList( new String[] { "/org/apache/maven/.*" } ) );

        targetRegistry.addRepositoryTarget( t2 );

        Target t3 =
            new Target( "maven1", "Maven1", maven1, Arrays.asList( new String[] { "/org\\.apache\\.maven.*" } ) );

        targetRegistry.addRepositoryTarget( t3 );

        applicationConfiguration.saveConfiguration();
    }

}
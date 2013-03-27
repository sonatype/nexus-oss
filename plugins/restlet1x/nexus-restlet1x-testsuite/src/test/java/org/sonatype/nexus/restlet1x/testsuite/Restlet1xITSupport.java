package org.sonatype.nexus.restlet1x.testsuite;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;

public class Restlet1xITSupport
    extends NexusRunningParametrizedITSupport
{

    public Restlet1xITSupport( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        return firstAvailableTestParameters(
            systemTestParameters(),
            testParameters(
                $( "${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle" )
            )
        ).load();
    }

    public static String uniqueName( final String prefix )
    {
        return prefix + "-" + new SimpleDateFormat( "yyyyMMdd-HHmmss-SSS" ).format( new Date() );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        configuration.addPlugins( artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-restlet1x-testsupport-plugin" ) );

        return configuration;
    }

    public Repositories repositories()
    {
        return client().getSubsystem( Repositories.class );
    }
}

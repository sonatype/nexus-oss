package org.sonatype.nexus.integrationtests.nexus3638;

import java.net.URL;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Downloader
    extends Thread
{

    private Throwable[] errors;

    private int i;

    private Gav gav;

    private Nexus3638IndexProxiedMavenPluginIT it;

    public Downloader( Nexus3638IndexProxiedMavenPluginIT it, Gav gav, int i, Throwable[] errors )
    {
        this.gav = gav;
        this.i = i;
        this.errors = errors;
        this.it = it;
    }

    @Override
    public void run()
    {
        try
        {
            // it.downloadSnapshotArtifact( "nexus3638", gav, new File( "target/downloads/nexus3638/" + i ) );
            it.downloadFile(
                new URL( AbstractNexusIntegrationTest.nexusBaseUrl
                    + AbstractNexusIntegrationTest.REPOSITORY_RELATIVE_URL + "nexus3638"
                        + "/org/apache/maven/plugins/maven-invoker-plugin/1.6-SNAPSHOT/maven-invoker-plugin-1.6-20100922.124315-3.jar" ),
                "target/downloads/nexus3638" );

        }
        catch ( Throwable t )
        {
            errors[i] = t;
        }
    }

}

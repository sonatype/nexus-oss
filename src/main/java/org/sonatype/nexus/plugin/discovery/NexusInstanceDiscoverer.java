package org.sonatype.nexus.plugin.discovery;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

public interface NexusInstanceDiscoverer
{

    NexusConnectionInfo discover( final Settings settings, final MavenProject project, final boolean fullyAutomatic )
        throws NexusDiscoveryException;

    NexusConnectionInfo fillAuth( final String nexusUrl, final Settings settings,
                                  final MavenProject project, final boolean fullyAutomatic )
        throws NexusDiscoveryException;

}

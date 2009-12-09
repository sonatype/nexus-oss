package org.sonatype.nexus.plugin.discovery;

import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

public interface NexusInstanceDiscoverer
{

    NexusConnectionInfo discover( final Settings settings, final MavenProject project, final String defaultUser,
                                  final boolean fullyAutomatic )
        throws NexusDiscoveryException;

    NexusConnectionInfo fillAuth( final String nexusUrl, final Settings settings, final MavenProject project,
                                  final String defaultUser, final boolean fullyAutomatic )
        throws NexusDiscoveryException;
    
    SecDispatcher getSecDispatcher();
    
    void setSecDispatcher( SecDispatcher secDispatcher );

}

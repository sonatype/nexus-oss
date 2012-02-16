package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;

/**
 * Disables the shutdown hook. Usable in tests.
 * 
 * @author cstamas
 */
public class DisableShutdownHookMangler
    implements ServerMangler<Server>
{
    public Server mangle( final Server server )
    {
        server.setStopAtShutdown( false );

        return server;
    }
}

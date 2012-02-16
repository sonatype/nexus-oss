package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;

/**
 * Returns the server instance to the caller.
 * 
 * @author cstamas
 */
public class JettyGetterMangler
    implements ServerMangler<Server>
{
    public Server mangle( final Server server )
    {
        return server;
    }
}

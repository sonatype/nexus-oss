package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Gets the ContextHandler on given path.
 * 
 * @author cstamas
 */
public class ContextGetterMangler
    extends AbstractContextMangler
    implements ServerMangler<ContextHandler>
{
    public ContextGetterMangler( final String contextPath )
    {
        super( contextPath );
    }

    public ContextHandler mangle( final Server server )
    {
        return getContext( server );
    }
}

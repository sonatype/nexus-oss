package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

/**
 * Abstract base class for manglers working with contexts.
 * 
 * @author cstamas
 */
public abstract class AbstractContextMangler
{
    private final String contextPath;

    protected AbstractContextMangler( final String contextPath )
    {
        this.contextPath = contextPath;
    }

    protected ContextHandler getContext( final Server server )
    {
        Handler[] handlers = server.getHandlers();
        if ( handlers == null )
        {
            handlers = new Handler[] { server.getHandler() };
        }

        return getContextHandlerOnPath( contextPath, handlers );
    }

    // ==

    protected ContextHandler getContextHandlerOnPath( final String contextPath, final Handler[] handlers )
    {
        for ( int i = 0; i < handlers.length; i++ )
        {
            if ( handlers[i] instanceof ContextHandler )
            {
                ContextHandler ctx = (ContextHandler) handlers[i];

                if ( contextPath.equals( ctx.getContextPath() ) )
                {
                    return ctx;
                }
            }
            else if ( handlers[i] instanceof HandlerCollection )
            {
                Handler[] handlerList = ( (HandlerCollection) handlers[i] ).getHandlers();

                return getContextHandlerOnPath( contextPath, handlerList );
            }
        }

        return null;
    }
}

package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;

/**
 * Sets context attribute.
 * 
 * @author cstamas
 */
public class ContextAttributeSetterMangler
    implements ServerMangler<Object>
{
    private final String attributeKey;

    private final Object attribute;

    public ContextAttributeSetterMangler( final String attributeKey, final Object attribute )
    {
        this.attributeKey = attributeKey;
        this.attribute = attribute;
    }

    public Object mangle( final Server server )
    {
        Handler[] handlers = server.getHandlers();

        if ( handlers == null )
        {
            handlers = new Handler[] { server.getHandler() };
        }

        return setAppContextOnAllContextHandlers( handlers );
    }

    // ==

    protected Object setAppContextOnAllContextHandlers( final Handler[] handlers )
    {
        for ( int i = 0; i < handlers.length; i++ )
        {
            if ( handlers[i] instanceof ContextHandler )
            {
                ContextHandler ctx = (ContextHandler) handlers[i];

                ctx.setAttribute( attributeKey, attribute );
            }

            if ( handlers[i] instanceof HandlerCollection )
            {
                Handler[] handlerList = ( (HandlerCollection) handlers[i] ).getHandlers();

                setAppContextOnAllContextHandlers( handlerList );
            }
        }

        return null;
    }
}

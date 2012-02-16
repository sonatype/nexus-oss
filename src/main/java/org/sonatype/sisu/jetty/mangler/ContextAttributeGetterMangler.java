package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

/**
 * Sets context attribute.
 * 
 * @author cstamas
 */
public class ContextAttributeGetterMangler
    extends AbstractContextMangler
    implements ServerMangler<Object>
{
    private final String attributeKey;

    public ContextAttributeGetterMangler( final String contextPath, final String attributeKey )
    {
        super( contextPath );
        this.attributeKey = attributeKey;
    }

    public Object mangle( final Server server )
    {
        ContextHandler ctx = getContext( server );

        if ( ctx != null && ctx.getServletContext() != null )
        {
            // try with servlet context is available, it falls back to attributes anyway
            return ctx.getServletContext().getAttribute( attributeKey );
        }
        else if ( ctx != null )
        {
            // try plain jetty attributes
            return ctx.getAttribute( attributeKey );
        }
        else
        {
            return null;
        }
    }
}

/*
 * Copyright (c) 2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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

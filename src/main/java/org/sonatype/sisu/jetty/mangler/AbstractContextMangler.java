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

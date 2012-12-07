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
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Mangler that sets UnavailableOnStartupException on all WebAppContexts to true (or to defined value). It does so by
 * "crawling" all the defined contexts in Jetty instance. Note: use of this handler is limited only before Jetty is
 * started (for obvious reasons)! It returns the count of affected WebAppContext encountered during it's work being done
 * (in other words, returns the count of the setter invocations it did).
 * 
 * @author cstamas
 * @since 1.2
 */
public class UnavailableOnStartupExceptionContextMangler
    implements ServerMangler<Integer>
{
    private final boolean throwUnavailableOnStartupException;

    public UnavailableOnStartupExceptionContextMangler()
    {
        this( true );
    }

    public UnavailableOnStartupExceptionContextMangler( final boolean throwUnavailableOnStartupException )
    {
        this.throwUnavailableOnStartupException = throwUnavailableOnStartupException;
    }

    public Integer mangle( Server server )
    {
        return setUnavailableOnStartupException( server.getHandlers() );
    }

    // ==

    protected int setUnavailableOnStartupException( final Handler[] handlers )
    {
        int setCount = 0;
        for ( int i = 0; i < handlers.length; i++ )
        {
            if ( handlers[i] instanceof ContextHandler )
            {
                final ContextHandler ctx = (ContextHandler) handlers[i];

                if ( ctx instanceof WebAppContext )
                {
                    final WebAppContext wctx = (WebAppContext) ctx;
                    wctx.setThrowUnavailableOnStartupException( throwUnavailableOnStartupException );
                    setCount++;
                }
            }
            else if ( handlers[i] instanceof HandlerCollection )
            {
                final Handler[] handlerList = ( (HandlerCollection) handlers[i] ).getHandlers();
                setCount = setCount + setUnavailableOnStartupException( handlerList );
            }
        }
        return setCount;
    }
}

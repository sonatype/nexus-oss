/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.appcontext.internal;

import java.util.concurrent.CopyOnWriteArrayList;

import org.sonatype.appcontext.lifecycle.AppContextLifecycleManager;
import org.sonatype.appcontext.lifecycle.LifecycleHandler;

public class AppContextLifecycleManagerImpl
    implements AppContextLifecycleManager
{
    private final CopyOnWriteArrayList<LifecycleHandler> handlers;

    public AppContextLifecycleManagerImpl()
    {
        this.handlers = new CopyOnWriteArrayList<LifecycleHandler>();
    }

    public void registerManaged( final LifecycleHandler handler )
    {
        handlers.add( handler );
    }

    public void unregisterManaged( final LifecycleHandler handler )
    {
        handlers.remove( handler );
    }

    public void invokeHandler( final Class<? extends LifecycleHandler> clazz )
    {
        for ( LifecycleHandler handler : handlers )
        {
            if ( clazz.isAssignableFrom( handler.getClass() ) )
            {
                try
                {
                    handler.handle();
                }
                catch ( Exception e )
                {
                    // nop
                }
            }
        }
    }
}

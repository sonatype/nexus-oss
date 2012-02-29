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

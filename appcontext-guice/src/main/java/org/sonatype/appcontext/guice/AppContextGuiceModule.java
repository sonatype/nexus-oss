package org.sonatype.appcontext.guice;

import org.sonatype.appcontext.AppContext;

import com.google.inject.AbstractModule;

/**
 * Guice Module exposing AppContext as component, binding it to {@code AppContext.class} key.
 * 
 * @author cstamas
 * @since 3.1
 */
public class AppContextGuiceModule
    extends AbstractModule
{
    private final AppContext appContext;

    public AppContextGuiceModule( final AppContext appContext )
    {
        if ( appContext == null )
        {
            throw new NullPointerException( "AppContext instance cannot be null!" );
        }
        this.appContext = appContext;
    }

    protected AppContext getAppContext()
    {
        return appContext;
    }

    @Override
    protected void configure()
    {
        bind( AppContext.class ).toInstance( getAppContext() );
    }
}

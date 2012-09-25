package org.sonatype.appcontext.guice;

import org.sonatype.appcontext.AppContext;
import org.sonatype.guice.bean.binders.ParameterKeys;

import com.google.inject.AbstractModule;

/**
 * Guice Module exposing AppContext as component, binding it to {@code AppContext.class} key. Word of warning: this
 * class is not suitable in all cases, like Nexus for example, as the appcontext is at "top level" Jetty classpath where
 * no Guice exists. Hence, Nexus for example "reimplements" this same module to avoid class not found related problems.
 * It really depends how you use AppContext.
 * 
 * @author cstamas
 * @since 3.1
 */
public class AppContextModule
    extends AbstractModule
{
    private final AppContext appContext;

    public AppContextModule( final AppContext appContext )
    {
        if ( appContext == null )
        {
            throw new NullPointerException( "AppContext instance cannot be null!" );
        }
        this.appContext = appContext;
    }

    @Override
    protected void configure()
    {
        bind( AppContext.class ).toInstance( appContext );
        bind( ParameterKeys.PROPERTIES ).toInstance( new StringAdapter( appContext ) );
    }
}

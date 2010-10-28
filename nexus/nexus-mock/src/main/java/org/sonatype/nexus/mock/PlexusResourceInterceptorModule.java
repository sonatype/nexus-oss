package org.sonatype.nexus.mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.sonatype.plexus.rest.resource.PlexusResource;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;

@Named
final class PlexusResourceInterceptorModule
    extends AbstractModule
{
    static final List<String> INTERCEPTED_METHODS = Arrays.asList( "get", "delete", "put", "post", "upload" );

    @Override
    protected void configure()
    {
        bindInterceptor( Matchers.subclassesOf( PlexusResource.class ), new AbstractMatcher<Method>()
        {
            public boolean matches( Method m )
            {
                return INTERCEPTED_METHODS.contains( m.getName() );
            }
        }, new PlexusResourceInterceptor() );
    }
}

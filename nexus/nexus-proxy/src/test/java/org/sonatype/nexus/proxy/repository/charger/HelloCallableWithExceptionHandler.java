package org.sonatype.nexus.proxy.repository.charger;

public class HelloCallableWithExceptionHandler
    extends HelloCallable
    implements ExceptionHandler
{
    public HelloCallableWithExceptionHandler( final String name )
    {
        super( name );
    }

    @Override
    public boolean handle( Exception ex )
    {
        // nop
        return false;
    }
}

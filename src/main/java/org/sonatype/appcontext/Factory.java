package org.sonatype.appcontext;

import org.sonatype.appcontext.internal.InternalFactory;

public class Factory
{
    public static AppContextRequest getDefaultRequest()
    {
        return getDefaultRequest( "default" );
    }

    public static AppContextRequest getDefaultRequest( final String id )
    {
        return getDefaultRequest( id, null );
    }

    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent )
    {
        return InternalFactory.getDefaultAppContextRequest( id, parent );
    }

    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
        return InternalFactory.create( request );
    }
}

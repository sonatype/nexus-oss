package org.sonatype.appcontext;

import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.InternalFactory;

public class Factory
{
    private static final List<String> EMPTY = Collections.emptyList();

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
        return getDefaultRequest( id, parent, EMPTY );
    }

    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent,
                                                       final List<String> aliases )
    {
        return InternalFactory.getDefaultAppContextRequest( id, parent, aliases );
    }

    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
        return InternalFactory.create( request );
    }
}

package org.sonatype.appcontext;

import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.InternalFactory;
import org.sonatype.appcontext.publisher.PrintStreamEntryPublisher;
import org.sonatype.appcontext.publisher.Slf4jLoggerEntryPublisher;
import org.sonatype.appcontext.source.Sources;

public class Factory
{
    private static final List<String> EMPTY = Collections.emptyList();

    /**
     * Creates a "default" request, with all the default sources and publishers. The request will have ID "default",
     * default sources are coming from {@link Sources#getDefaultSources(String, List)} and
     * {@link PrintStreamEntryPublisher} or {@link Slf4jLoggerEntryPublisher} publisher, depending is SLF4J detected on
     * class path or not.
     * 
     * @return
     */
    public static AppContextRequest getDefaultRequest()
    {
        return getDefaultRequest( "default" );
    }

    /**
     * Creates a "default" request with given ID. See {@link #getDefaultRequest()} for sources and publishers.
     * 
     * @param id
     * @return
     */
    public static AppContextRequest getDefaultRequest( final String id )
    {
        return getDefaultRequest( id, null );
    }

    /**
     * Creates a "default" request with given ID and given parent app context. See {@link #getDefaultRequest()} for
     * sources and publishers.
     * 
     * @param id
     * @param parent
     * @return
     */
    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent )
    {
        return getDefaultRequest( id, parent, EMPTY );
    }

    /**
     * Creates a "default" request with given ID and given parent app context and given "aliases" (aliases are used in
     * harvesting the sources, for prefix matching only). See {@link #getDefaultRequest()} for sources and publishers.
     * 
     * @param id
     * @param parent
     * @return
     */
    public static AppContextRequest getDefaultRequest( final String id, final AppContext parent,
                                                       final List<String> aliases, final String... keyInclusions )
    {
        return InternalFactory.getDefaultAppContextRequest( id, parent, aliases, keyInclusions );
    }

    /**
     * Creates AppContext instance from the given request.
     * 
     * @param request
     * @return
     * @throws AppContextException
     */
    public static AppContext create( final AppContextRequest request )
        throws AppContextException
    {
        return InternalFactory.create( request );
    }
}

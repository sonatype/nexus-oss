package org.sonatype.appcontext;

import java.util.Map;

/**
 * A context filler that uses plain Java Map as source.
 * 
 * @author cstamas
 */
public class MapSourcedContextFiller
    implements ContextFiller
{
    private final Map<Object, Object> map;

    public MapSourcedContextFiller( Map<Object, Object> map )
    {
        this.map = map;
    }

    public void fillContext( AppContextRequest request, Map<Object, Object> context )
        throws AppContextException
    {
        context.putAll( map );
    }
}

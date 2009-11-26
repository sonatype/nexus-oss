package org.sonatype.appcontext;

import java.util.Map;

/**
 * A context filler that another AppContext as context source. This filler should be set-up before using!
 * 
 * @author cstamas
 */
public class AppContextContextFiller
    implements ContextFiller
{
    private AppContext source;

    public AppContextContextFiller( AppContext source )
    {
        this.source = source;
    }

    public void fillContext( AppContextFactory factory, AppContextRequest request, Map<Object, Object> context )
        throws AppContextException
    {
        // just dump it in
        for ( Map.Entry<Object, Object> entry : source.entrySet() )
        {
            context.put( entry.getKey(), entry.getValue() );
        }
    }

    public AppContext getSource()
    {
        return source;
    }

    public void setSource( AppContext source )
    {
        this.source = source;
    }
}

package org.sonatype.appcontext;

import java.util.Collections;
import java.util.Map;

public class DefaultAppContextResponse
    implements AppContextResponse
{
    private final String name;

    private final Map<Object, Object> context;

    private final Map<Object, Object> rawContext;

    public DefaultAppContextResponse( String name, Map<Object, Object> context, Map<Object, Object> rawContext )
    {
        this.name = name;

        this.context = context;

        this.rawContext = rawContext;
    }

    public String getName()
    {
        return name;
    }

    public Map<Object, Object> getContext()
    {
        return Collections.unmodifiableMap( context );
    }

    public Map<Object, Object> getRawContext()
    {
        return Collections.unmodifiableMap( rawContext );
    }
}

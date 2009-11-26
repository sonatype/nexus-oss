package org.sonatype.appcontext;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class DefaultAppContextResponse
    implements AppContextResponse
{
    private final String name;
    
    private final File basedir;

    private final Map<Object, Object> context;

    private final Map<Object, Object> rawContext;

    public DefaultAppContextResponse( String name, File basedir, Map<Object, Object> context, Map<Object, Object> rawContext )
    {
        this.name = name;
        
        this.basedir = basedir;

        this.context = context;

        this.rawContext = rawContext;
    }

    public String getName()
    {
        return name;
    }
    
    public File getBasedir()
    {
        return basedir;
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

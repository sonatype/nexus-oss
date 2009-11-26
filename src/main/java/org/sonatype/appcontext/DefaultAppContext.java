package org.sonatype.appcontext;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.interpolation.Interpolator;

public class DefaultAppContext
    extends HashMap<Object, Object>
    implements AppContext
{
    private static final long serialVersionUID = -8789514933779543200L;

    private final AppContextFactory factory;

    private final String name;

    private final File basedir;

    private final Map<Object, Object> rawContext;

    public DefaultAppContext( AppContextFactory factory, String name, File basedir, Map<Object, Object> context,
        Map<Object, Object> rawContext )
    {
        this.factory = factory;

        this.name = name;

        this.basedir = basedir;

        this.rawContext = Collections.unmodifiableMap( rawContext );

        putAll( context );
    }

    public AppContextFactory getFactory()
    {
        return factory;
    }

    public String getName()
    {
        return name;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public Map<Object, Object> getRawContext()
    {
        return rawContext;
    }

    public Interpolator getInterpolator()
    {
        return getFactory().getInterpolator( this );
    }
}

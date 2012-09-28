package org.sonatype.appcontext.internal;

import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class RawAppContextValueSource
    extends AbstractValueSource
{
    private final AppContext context;

    public RawAppContextValueSource( final AppContext context )
    {
        super( false );
        this.context = context;
    }

    public Object getValue( String expression )
    {
        final AppContextEntry entry = context.getAppContextEntry( expression );

        if ( entry != null )
        {
            return entry.getRawValue();
        }
        else
        {
            return null;
        }
    }
}

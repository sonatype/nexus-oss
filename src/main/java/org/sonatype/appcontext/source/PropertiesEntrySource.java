package org.sonatype.appcontext.source;

import java.util.Properties;

import org.sonatype.appcontext.internal.Preconditions;

/**
 * EntrySource that sources itself from a {@code java.util.Properties} file. It might be set to fail but also to keep
 * silent the fact that file to load is not found.
 * 
 * @author cstamas
 */
public class PropertiesEntrySource
    extends AbstractMapEntrySource
{
    private final Properties source;

    public PropertiesEntrySource( final String name, final Properties source )
    {
        super( name, "props" );
        this.source = Preconditions.checkNotNull( source );
    }

    protected Properties getSource()
    {
        return source;
    }
}

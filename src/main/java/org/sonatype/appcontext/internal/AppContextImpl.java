package org.sonatype.appcontext.internal;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.AppContextInterpolationException;

public class AppContextImpl
    implements AppContext
{
    private final long created;

    private long modified;

    private final String id;

    private final AppContext parent;

    private final HierarchicalMap<String, AppContextEntry> entries;

    public AppContextImpl( final long created, final String id, final AppContextImpl parent,
                           final Map<String, AppContextEntry> sourcedEntries )
    {
        this.created = created;
        this.modified = this.created;
        this.id = Preconditions.checkNotNull( id );

        this.parent = parent;

        if ( parent != null )
        {
            this.entries = new HierarchicalMap<String, AppContextEntry>( parent.getEntries() );
        }
        else
        {
            this.entries = new HierarchicalMap<String, AppContextEntry>();
        }

        this.entries.putAll( sourcedEntries );
    }

    public long getCreated()
    {
        return created;
    }

    public long getModified()
    {
        return modified;
    }

    public String getId()
    {
        return id;
    }

    public AppContext getParent()
    {
        return parent;
    }

    public AppContextEntry getAppContextEntry( String key )
    {
        return entries.get( key );
    }

    public Map<String, Object> flatten()
    {
        final Map<String, AppContextEntry> flattenEntries = flattenAppContextEntries();

        final HashMap<String, Object> result = new HashMap<String, Object>( flattenEntries.size() );

        for ( AppContextEntry entry : flattenEntries.values() )
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return Collections.unmodifiableMap( result );
    }

    public Map<String, AppContextEntry> flattenAppContextEntries()
    {
        final HashMap<String, AppContextEntry> result = new HashMap<String, AppContextEntry>();

        result.putAll( entries.flatten() );

        return Collections.unmodifiableMap( result );
    }

    @Deprecated
    public Interpolator getInterpolator()
    {
        return InternalFactory.getInterpolator( this );
    }

    public String interpolate( final String string )
        throws AppContextInterpolationException
    {
        try
        {
            return InternalFactory.getInterpolator( this ).interpolate( string );
        }
        catch ( InterpolationException e )
        {
            throw new AppContextInterpolationException( e.getMessage(), e );
        }
    }

    public void dump()
    {
        dump( System.out );
    }

    public void dump( final PrintStream ps )
    {
        if ( parent != null )
        {
            parent.dump( ps );
        }

        // now dump me
        ps.println( "AppContext: " + getId() );
        for ( AppContextEntry entry : entries.values() )
        {
            ps.println( entry.toString() );
        }
        ps.println();
    }

    // ==

    protected HierarchicalMap<String, AppContextEntry> getEntries()
    {
        return entries;
    }

    public AppContextEntry put( AppContextEntry entry )
    {
        final AppContextEntry oldEntry = entries.put( Preconditions.checkNotNull( entry ).getKey(), entry );

        if ( oldEntry != null )
        {
            return oldEntry;
        }
        else
        {
            return null;
        }
    }

    protected void markContextModified( final long timestamp )
    {
        this.modified = timestamp;
    }

    // ==

    public int size()
    {
        return entries.size();
    }

    public boolean isEmpty()
    {
        return entries.isEmpty();
    }

    public boolean containsKey( Object key )
    {
        return entries.containsKey( key );
    }

    public boolean containsValue( Object value )
    {
        return entries.containsValue( value );
    }

    public Object get( Object key )
    {
        final AppContextEntry entry = entries.get( key );

        if ( entry != null )
        {
            return entry.getValue();
        }
        else
        {
            return null;
        }
    }

    public Object put( String key, Object value )
    {
        final long created = System.currentTimeMillis();

        final AppContextEntry oldEntry =
            put( new AppContextEntryImpl( created, key, value, value, new ProgrammaticallySetSourceMarker() ) );

        markContextModified( created );

        if ( oldEntry != null )
        {
            return oldEntry.getValue();
        }
        else
        {
            return null;
        }
    }

    public Object remove( Object key )
    {
        final AppContextEntry oldEntry = entries.remove( key );

        markContextModified( System.currentTimeMillis() );

        if ( oldEntry != null )
        {
            return oldEntry.getValue();
        }
        else
        {
            return null;
        }
    }

    public void putAll( Map<? extends String, ? extends Object> m )
    {
        for ( Map.Entry<? extends String, ? extends Object> e : m.entrySet() )
        {
            put( e.getKey(), e.getValue() );
        }
    }

    public void clear()
    {
        entries.clear();
        markContextModified( System.currentTimeMillis() );
    }

    public Set<String> keySet()
    {
        return Collections.unmodifiableSet( entries.keySet() );
    }

    public Collection<Object> values()
    {
        final ArrayList<Object> result = new ArrayList<Object>( entries.size() );

        for ( Map.Entry<String, AppContextEntry> entry : entries.entrySet() )
        {
            result.add( entry.getValue().getValue() );
        }

        return Collections.unmodifiableList( result );
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet()
    {
        final Map<String, Object> result = new HashMap<String, Object>( entries.size() );

        for ( Map.Entry<String, AppContextEntry> entry : entries.entrySet() )
        {
            result.put( entry.getKey(), entry.getValue().getValue() );
        }

        return Collections.unmodifiableSet( result.entrySet() );
    }

    // ==

    public String toString()
    {
        return entries.values().toString();
    }
}

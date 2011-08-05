package org.sonatype.appcontext.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.interpolation.Interpolator;
import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class AppContextImpl
    implements AppContext
{
    private final String id;

    private final AppContext parent;

    private final HierarchicalMap<String, AppContextEntry> entries;

    public AppContextImpl( final String id, final AppContextImpl parent,
                           final Map<String, AppContextEntry> sourcedEntries )
    {
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
        final HashMap<String, Object> result = new HashMap<String, Object>();

        result.putAll( entries.flatten() );

        return result;
    }

    public Interpolator getInterpolator()
    {
        return InternalFactory.getInterpolator( this );
    }

    public void dump()
    {
        if ( parent != null )
        {
            parent.dump();
        }

        // now dump me
        System.out.println( "AppContext: " + getId() );
        for ( AppContextEntry entry : entries.values() )
        {
            System.out.println( entry.toString() );
        }
        System.out.println();
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
        final AppContextEntry oldEntry =
            put( new AppContextEntryImpl( key, value, value, new ProgrammaticallySetSourceMarker() ) );

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
    }

    public Set<String> keySet()
    {
        return entries.keySet();
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

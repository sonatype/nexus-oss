package org.sonatype.appcontext.guice;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.guice.bean.binders.ParameterKeys;

/**
 * Simple adapter to adapt {@link AppContext} into a {@link Map} of strings, usable for SISU
 * {@link ParameterKeys#PROPERTIES} bindings. Since {@link AppContext} values are Objects, their type will be checked
 * before serving it up, simply denying the existence of any non-string value (and their corresponding keys).
 * 
 * @author cstamas
 */
public class StringMapAdapter
    implements Map<String, String>
{
    private final AppContext appContext;

    public StringMapAdapter( final AppContext appContext )
    {
        this.appContext = Preconditions.checkNotNull( appContext );
    }

    public void clear()
    {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey( Object key )
    {
        return get( key ) != null;
    }

    public boolean containsValue( Object value )
    {
        return value instanceof String && appContext.containsValue( value );
    }

    public Set<Map.Entry<String, String>> entrySet()
    {
        throw new UnsupportedOperationException();
    }

    public String get( Object key )
    {
        final Object result = appContext.get( key );
        if ( result instanceof String )
        {
            return (String) result;
        }
        else
        {
            return null;
        }
    }

    public boolean isEmpty()
    {
        return appContext.flatten().isEmpty();
    }

    public Set<String> keySet()
    {
        return appContext.flatten().keySet();
    }

    public String put( String key, String value )
    {
        throw new UnsupportedOperationException();
    }

    public void putAll( Map<? extends String, ? extends String> m )
    {
        throw new UnsupportedOperationException();
    }

    public String remove( Object key )
    {
        throw new UnsupportedOperationException();
    }

    public int size()
    {
        return appContext.flatten().size();
    }

    public Collection<String> values()
    {
        throw new UnsupportedOperationException();
    }
}

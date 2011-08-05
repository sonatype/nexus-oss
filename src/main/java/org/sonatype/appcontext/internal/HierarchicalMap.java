package org.sonatype.appcontext.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Map (based on HashMap, so all of it's internalities applies), that might have a parent. Parent is referred only in
 * case of query operations ({@link #get(Object)}, {@link #containsKey(Object)}, {@link #containsValue(Object)}), so for
 * example the {@link #get(Object)} method may "bubble" up on multiple ancestors to grab a value. Still, even if current
 * map has no entries, but it serves entries from it's parent, {@link #size()} of this map will return 0.
 * 
 * @author cstamas
 * @param <K>
 * @param <V>
 */
public class HierarchicalMap<K, V>
    extends ConcurrentHashMap<K, V>
    implements Map<K, V>
{
    private static final long serialVersionUID = 3445870461584217031L;

    private final HierarchicalMap<K, V> parent;

    public HierarchicalMap()
    {
        this( null );
    }

    public HierarchicalMap( final HierarchicalMap<K, V> parent )
    {
        super();

        this.parent = checkParentContext( parent );
    }

    public HierarchicalMap<K, V> getParent()
    {
        return parent;
    }

    protected HierarchicalMap<K, V> checkParentContext( HierarchicalMap<K, V> context )
    {
        if ( context != null )
        {
            if ( this == context )
            {
                throw new IllegalArgumentException(
                    "The context cannot be parent of itself! The parent instance cannot equals to this instance!" );
            }

            HierarchicalMap<K, V> otherParentContext = context.getParent();

            while ( otherParentContext != null )
            {
                if ( this == otherParentContext )
                {
                    throw new IllegalArgumentException( "The context cannot be an ancestor of itself! Cycle detected!" );
                }

                otherParentContext = otherParentContext.getParent();
            }
        }

        return context;
    }

    @Override
    public boolean containsKey( Object key )
    {
        return containsKey( key, true );
    }

    public boolean containsKey( Object key, boolean fallBackToParent )
    {
        boolean result = super.containsKey( key );

        if ( fallBackToParent && !result && getParent() != null )
        {
            result = getParent().containsKey( key );
        }

        return result;
    }

    @Override
    public boolean containsValue( Object val )
    {
        return containsValue( val, true );
    }

    public boolean containsValue( Object val, boolean fallBackToParent )
    {
        boolean result = super.containsValue( val );

        if ( fallBackToParent && !result && getParent() != null )
        {
            result = getParent().containsValue( val );
        }

        return result;
    }

    @Override
    public V get( Object key )
    {
        return get( key, true );
    }

    public V get( Object key, boolean fallBackToParent )
    {
        if ( containsKey( key, false ) )
        {
            return super.get( key );
        }
        else if ( fallBackToParent && getParent() != null )
        {
            return getParent().get( key );
        }
        else
        {
            return null;
        }
    }

    // ==

    public Map<K, V> flatten()
    {
        HashMap<K, V> result = new HashMap<K, V>();

        HierarchicalMap<K, V> ctx = this;

        Stack<HierarchicalMap<K, V>> stack = new Stack<HierarchicalMap<K, V>>();

        while ( ctx != null )
        {
            stack.push( ctx );

            ctx = ctx.getParent();
        }

        while ( !stack.isEmpty() )
        {
            ctx = stack.pop();

            result.putAll( ctx );
        }

        return result;
    }
}
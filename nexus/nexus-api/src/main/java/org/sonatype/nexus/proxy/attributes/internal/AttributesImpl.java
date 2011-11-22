package org.sonatype.nexus.proxy.attributes.internal;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageItem;

import com.google.common.base.Preconditions;

public class AttributesImpl
    extends HashMap<String, String>
    implements Attributes
{
    private static final long serialVersionUID = 879646089367097825L;

    public AttributesImpl()
    {
        super();
    }

    public AttributesImpl( final Map<String, String> m )
    {
        super( m );
    }

    protected int getInteger( final String key, final int defaultValue )
    {
        if ( containsKey( key ) )
        {
            return Integer.valueOf( get( key ) );
        }
        else
        {
            return defaultValue;
        }
    }

    protected void setInteger( final String key, final int value )
    {
        put( key, Integer.toString( value ) );
    }

    protected long getLong( final String key, final long defaultValue )
    {
        if ( containsKey( key ) )
        {
            return Long.valueOf( get( key ) );
        }
        else
        {
            return defaultValue;
        }
    }

    protected void setLong( final String key, final long value )
    {
        put( key, Long.toString( value ) );
    }

    protected boolean getBoolean( final String key, final boolean defaultValue )
    {
        if ( containsKey( key ) )
        {
            return Boolean.valueOf( get( key ) );
        }
        else
        {
            return defaultValue;
        }
    }

    protected void setBoolean( final String key, final boolean value )
    {
        put( key, Boolean.toString( value ) );
    }

    protected String getString( final String key, final String defaultValue )
    {
        if ( containsKey( key ) )
        {
            return get( key );
        }
        else
        {
            return defaultValue;
        }
    }

    protected void setString( final String key, final String value )
    {
        put( key, Preconditions.checkNotNull( value ) );
    }

    protected String getKeyForAttribute( final String attributeName )
    {
        // TODO: Jackson will use the key as XML element!
        return String.format( "%s-%s", StorageItem.class.getName(), Preconditions.checkNotNull( attributeName ) );
    }

    // ==

    @Override
    public void overlayAttributes( final Attributes repositoryItemAttributes )
    {
        putAll( repositoryItemAttributes );
//        for ( Map.Entry<String, String> entry : repositoryItemAttributes.entrySet() )
//        {
//            if ( !containsKey( entry.getKey() ) )
//            {
//                put( entry.getKey(), entry.getValue() );
//            }
//        }
    }

    @Override
    public int getGeneration()
    {
        return getInteger( getKeyForAttribute( "generation" ), 0 );
    }

    @Override
    public void setGeneration( final int value )
    {
        setInteger( getKeyForAttribute( "generation" ), value );
    }

    @Override
    public void incrementGeneration()
    {
        setInteger( getKeyForAttribute( "generation" ), getGeneration() + 1 );
    }

    @Override
    public String getPath()
    {
        return getString( getKeyForAttribute( "path" ), null );
    }

    @Override
    public void setPath( final String value )
    {
        setString( getKeyForAttribute( "path" ), value );
    }

    @Override
    public boolean isReadable()
    {
        return getBoolean( getKeyForAttribute( "readable" ), true );
    }

    @Override
    public void setReadable( final boolean value )
    {
        setBoolean( getKeyForAttribute( "readable" ), value );
    }

    @Override
    public boolean isWritable()
    {
        return getBoolean( getKeyForAttribute( "writable" ), true );
    }

    @Override
    public void setWritable( final boolean value )
    {
        setBoolean( getKeyForAttribute( "writable" ), value );
    }

    @Override
    public String getRepositoryId()
    {
        return getString( getKeyForAttribute( "repositoryId" ), null );
    }

    @Override
    public void setRepositoryId( final String value )
    {
        setString( getKeyForAttribute( "repositoryId" ), value );
    }

    @Override
    public long getCreated()
    {
        return getLong( getKeyForAttribute( "created" ), 0 );
    }

    @Override
    public void setCreated( final long value )
    {
        setLong( getKeyForAttribute( "created" ), value );
    }

    @Override
    public long getModified()
    {
        return getLong( getKeyForAttribute( "modified" ), 0 );
    }

    @Override
    public void setModified( final long value )
    {
        setLong( getKeyForAttribute( "modified" ), value );
    }

    @Override
    public long getStoredLocally()
    {
        return getLong( getKeyForAttribute( "storedLocally" ), 0 );
    }

    @Override
    public void setStoredLocally( final long value )
    {
        setLong( getKeyForAttribute( "storedLocally" ), value );
    }

    @Override
    public long getCheckedRemotely()
    {
        return getLong( getKeyForAttribute( "checkedRemotely" ), 0 );
    }

    @Override
    public void setCheckedRemotely( final long value )
    {
        setLong( getKeyForAttribute( "checkedRemotely" ), value );
    }

    @Override
    public long getLastRequested()
    {
        return getLong( getKeyForAttribute( "lastRequested" ), 0 );
    }

    @Override
    public void setLastRequested( final long value )
    {
        setLong( getKeyForAttribute( "lastRequested" ), value );
    }

    @Override
    public boolean isExpired()
    {
        return getBoolean( getKeyForAttribute( "expired" ), false );
    }

    @Override
    public void setExpired( final boolean value )
    {
        setBoolean( getKeyForAttribute( "expired" ), value );
    }

    @Override
    public String getRemoteUrl()
    {
        return getString( getKeyForAttribute( "remoteUrl" ), null );
    }

    @Override
    public void setRemoteUrl( final String value )
    {
        setString( getKeyForAttribute( "remoteUrl" ), value );
    }

    @Override
    public long getLength()
    {
        return getLong( getKeyForAttribute( "length" ), 0 );
    }

    @Override
    public void setLength( final long value )
    {
        setLong( getKeyForAttribute( "length" ), value );
    }
}

package org.sonatype.nexus.configuration.application;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.storage.StorageContext;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class SimpleLocalStorageContext
    implements LocalStorageContext
{
    private Map<String, Object> ctx = new HashMap<String, Object>();

    public long getLastChanged()
    {
        return 0;
    }

    public RemoteStorageContext getParentStorageContext()
    {
        return null;
    }

    public Map<String, Object> getContext()
    {
        return ctx;
    }

    public Object getContextObject( String key )
    {
        return ctx.get( key );
    }

    public void putContextObject( String key, Object value )
    {
        ctx.put( key, value );
    }

    public void removeContextObject( String key )
    {
        ctx.remove( key );
    }

    public void setParentStorageContext( StorageContext parent )
    {
        // TODO Auto-generated method stub
        
    }

    public boolean hasContextObject( String key )
    {
        return false;
    }

}

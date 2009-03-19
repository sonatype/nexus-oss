package org.sonatype.nexus.configuration.application;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class SimpleRemoteStorageContext
    implements RemoteStorageContext
{
    private Map<String, Object> ctx = new HashMap<String, Object>();

    public long getLastChanged()
    {
        return 0;
    }

    public RemoteStorageContext getParentRemoteStorageContext()
    {
        return null;
    }

    public Map<String, Object> getRemoteConnectionContext()
    {
        return ctx;
    }

    public Object getRemoteConnectionContextObject( String key )
    {
        return ctx.get( key );
    }

    public void putRemoteConnectionContextObject( String key, Object value )
    {
        ctx.put( key, value );
    }

    public void removeRemoteConnectionContextObject( String key )
    {
        ctx.remove( key );
    }

    public void setLastChanged( long ts )
    {
    }

}

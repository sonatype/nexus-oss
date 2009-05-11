package org.sonatype.nexus.configuration.application;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
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

    public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteConnectionSettings getRemoteConnectionSettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteProxySettings getRemoteProxySettings()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
    {
        // TODO Auto-generated method stub
        
    }

    public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
    {
        // TODO Auto-generated method stub
        
    }

    public void setRemoteProxySettings( RemoteProxySettings settings )
    {
        // TODO Auto-generated method stub
        
    }

    public boolean hasRemoteAuthenticationSettings()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasRemoteConnectionContextObject( String key )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasRemoteConnectionSettings()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean hasRemoteProxySettings()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeRemoteAuthenticationSettings()
    {
        // TODO Auto-generated method stub
        
    }

    public void removeRemoteConnectionSettings()
    {
        // TODO Auto-generated method stub
        
    }

    public void removeRemoteProxySettings()
    {
        // TODO Auto-generated method stub
        
    }

}

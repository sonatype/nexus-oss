/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.application;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.StorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class SimpleRemoteStorageContext
    implements RemoteStorageContext
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

    public boolean hasContextObject( String key )
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

    public void setParentStorageContext( StorageContext parent )
    {
        // TODO Auto-generated method stub
        
    }

}

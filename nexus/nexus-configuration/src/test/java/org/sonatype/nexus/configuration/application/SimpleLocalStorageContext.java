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

import org.sonatype.nexus.proxy.storage.StorageContext;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class SimpleLocalStorageContext
    implements LocalStorageContext
{
    private Map<String, Object> ctx = new HashMap<String, Object>();

    @Override
    public long getLastChanged()
    {
        return 0;
    }

    @Override
    public int getGeneration()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public RemoteStorageContext getParentStorageContext()
    {
        return null;
    }

    public Map<String, Object> getContext()
    {
        return ctx;
    }

    @Override
    public Object getContextObject( String key )
    {
        return ctx.get( key );
    }

    @Override
    public void putContextObject( String key, Object value )
    {
        ctx.put( key, value );
    }

    @Override
    public void removeContextObject( String key )
    {
        ctx.remove( key );
    }

    @Override
    public void setParentStorageContext( StorageContext parent )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasContextObject( String key )
    {
        return false;
    }

}

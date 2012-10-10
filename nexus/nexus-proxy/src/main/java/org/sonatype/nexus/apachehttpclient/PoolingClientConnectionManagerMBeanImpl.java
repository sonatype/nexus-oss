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
package org.sonatype.nexus.apachehttpclient;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import javax.management.StandardMBean;

import org.apache.http.impl.conn.PoolingClientConnectionManager;

/**
 * Default {@link PoolingClientConnectionManagerMBean} implementation.
 *
 * @since 2.2
 */
class PoolingClientConnectionManagerMBeanImpl
    extends StandardMBean
    implements PoolingClientConnectionManagerMBean
{

    private final WeakReference<PoolingClientConnectionManager> reference;

    PoolingClientConnectionManagerMBeanImpl( final WeakReference<PoolingClientConnectionManager> reference )
    {
        super( PoolingClientConnectionManagerMBean.class, false );

        this.reference = reference;
    }

    @Override
    public boolean isInUse()
    {
        return reference.get() != null;
    }

    @Override
    public int getMaxTotal()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getMaxTotal();
    }

    @Override
    public int getDefaultMaxPerRoute()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getDefaultMaxPerRoute();
    }

    @Override
    public int getLeased()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getTotalStats().getLeased();
    }

    @Override
    public int getPending()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getTotalStats().getPending();
    }

    @Override
    public int getAvailable()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getTotalStats().getAvailable();
    }

    @Override
    public int getMax()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        return connMgr == null ? 0 : connMgr.getTotalStats().getMax();
    }

    @Override
    public void closeIdleConnections( final long idleTimeoutInMillis )
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        if ( connMgr == null )
        {
            throw new IllegalStateException( "Already released" );
        }
        connMgr.closeIdleConnections( idleTimeoutInMillis, TimeUnit.MILLISECONDS );
    }

    @Override
    public void closeExpiredConnections()
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        if ( connMgr == null )
        {
            throw new IllegalStateException( "Already released" );
        }
        connMgr.closeExpiredConnections();
    }

    @Override
    public void setMaxTotal( final int max )
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        if ( connMgr == null )
        {
            throw new IllegalStateException( "Already released" );
        }
        connMgr.setMaxTotal( max );
    }

    @Override
    public void setDefaultMaxPerRoute( final int max )
    {
        final PoolingClientConnectionManager connMgr = reference.get();
        if ( connMgr == null )
        {
            throw new IllegalStateException( "Already released" );
        }
        connMgr.setDefaultMaxPerRoute( max );
    }

}

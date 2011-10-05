/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.sisu.locks;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.guice.bean.reflect.Weak;

/**
 * Abstract {@link Locks} implementation; associates names with {@link ResourceLock}s.
 */
abstract class AbstractLocks
    implements Locks
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    protected static final String JMX_DOMAIN = "org.sonatype.sisu";

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ConcurrentMap<String, ResourceLock> resourceLocks = Weak.concurrentValues();

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    AbstractLocks()
    {
        try
        {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

            final String type = getClass().getSimpleName();
            final String hash = String.format( "0x%08X", new Integer( System.identityHashCode( this ) ) );
            final ObjectName name = ObjectName.getInstance( JMX_DOMAIN, properties( "type", type, "hash", hash ) );

            server.registerMBean( new DefaultLocksMBean( this ), name );
        }
        catch ( final Exception e )
        {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public final ResourceLock getResourceLock( final String name )
    {
        ResourceLock lock = resourceLocks.get( name );
        if ( null == lock )
        {
            final ResourceLock oldLock = resourceLocks.putIfAbsent( name, lock = createResourceLock( name ) );
            if ( null != oldLock )
            {
                return oldLock;
            }
        }
        return lock;
    }

    public final String[] getResourceNames()
    {
        return resourceLocks.keySet().toArray( new String[0] );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * @return Lock associated with the given resource name
     */
    protected abstract ResourceLock createResourceLock( final String name );

    protected static final Hashtable<String, String> properties( final String... keyValues )
    {
        final Hashtable<String, String> properties = new Hashtable<String, String>();
        for ( int i = 0; i < keyValues.length; )
        {
            properties.put( keyValues[i++], keyValues[i++] );
        }
        return properties;
    }
}

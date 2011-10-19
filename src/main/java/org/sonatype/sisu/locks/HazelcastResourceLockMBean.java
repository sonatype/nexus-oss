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

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.sonatype.guice.bean.reflect.Logs;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiTask;

/**
 * Distributed Hazelcast {@link ResourceLockMBean} implementation.
 */
final class HazelcastResourceLockMBean
    extends AbstractResourceLockMBean
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final HazelcastInstance instance;

    private final ObjectName jmxQuery;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    HazelcastResourceLockMBean( final HazelcastInstance instance, final ObjectName jmxQuery )
    {
        this.instance = instance;
        this.jmxQuery = jmxQuery;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String[] listResourceNames()
    {
        return invokeMethod( "listResourceNames" );
    }

    public String[] findOwningThreads( final String name )
    {
        return invokeMethod( "findOwningThreads", name );
    }

    public String[] findWaitingThreads( final String name )
    {
        return invokeMethod( "findWaitingThreads", name );
    }

    public String[] findOwnedResources( final String tid )
    {
        return invokeMethod( "findOwnedResources", tid );
    }

    public String[] findWaitedResources( final String tid )
    {
        return invokeMethod( "findWaitedResources", tid );
    }

    public void releaseResource( final String name )
    {
        invokeMethod( "releaseResource", name );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    public String[] invokeMethod( final String method, final String... args )
    {
        final HazelcastMBeansInvoker invoker = new HazelcastMBeansInvoker( jmxQuery, method, args );
        final MultiTask<List<String>> task = new MultiTask<List<String>>( invoker, filterMembers( method, args ) );
        final Set<String> values = new HashSet<String>();
        try
        {
            instance.getExecutorService().execute( task );
            for ( final List<String> result : task.get() )
            {
                values.addAll( result );
            }
        }
        catch ( final Exception e )
        {
            Logs.warn( "Problem executing cluster MultiTask for: \"{}\"", method, e );
        }
        return values.toArray( new String[values.size()] );
    }

    private Set<Member> filterMembers( final String method, final String... args )
    {
        final Set<Member> members = new HashSet<Member>();
        try
        {
            if ( method.endsWith( "Resources" ) && args[0].contains( "@" ) )
            {
                final String[] tokens = args[0].split( "\\s*[@:]\\s*", 3 );

                args[0] = tokens[0];

                if ( tokens.length == 3 )
                {
                    final InetSocketAddress addr = new InetSocketAddress( tokens[1], Integer.parseInt( tokens[2] ) );
                    for ( final Member m : instance.getCluster().getMembers() )
                    {
                        if ( addr.equals( m.getInetSocketAddress() ) )
                        {
                            members.add( m );
                        }
                    }
                }
                else if ( tokens.length == 2 )
                {
                    final InetAddress addr = InetAddress.getByName( tokens[1] );
                    for ( final Member m : instance.getCluster().getMembers() )
                    {
                        if ( addr.equals( m.getInetSocketAddress().getAddress() ) )
                        {
                            members.add( m );
                        }
                    }
                }
            }
        }
        catch ( final Exception e )
        {
            Logs.warn( "Problem filtering cluster members for: \"{}\"", method, e );
        }
        if ( members.isEmpty() )
        {
            members.addAll( instance.getCluster().getMembers() );
        }
        return members;
    }
}

@SuppressWarnings( "serial" )
final class HazelcastMBeansInvoker
    implements HazelcastInstanceAware, Callable<List<String>>, Serializable
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private HazelcastInstance instance;

    private final ObjectName jmxQuery;

    private final String method;

    private final String[] args;

    // ----------------------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------------------

    HazelcastMBeansInvoker( final ObjectName jmxQuery, final String method, final String... args )
    {
        this.jmxQuery = jmxQuery;
        this.method = method;
        this.args = args;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void setHazelcastInstance( final HazelcastInstance instance )
    {
        this.instance = instance;
    }

    @Override
    public List<String> call()
        throws Exception
    {
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        final String[] sig = new String[args.length];
        Arrays.fill( sig, String.class.getName() );

        final List<String> values = new ArrayList<String>();
        for ( final ObjectName mBean : server.queryNames( jmxQuery, null ) )
        {
            try
            {
                final Object result = server.invoke( mBean, method, args, sig );
                if ( result instanceof String[] )
                {
                    Collections.addAll( values, (String[]) result );
                }
            }
            catch ( final Exception e )
            {
                Logs.warn( "Problem invoking JMX method: \"{}\"", method, e );
            }
        }
        return normalizeValues( values );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private List<String> normalizeValues( final List<String> values )
    {
        if ( method.endsWith( "Threads" ) )
        {
            final String addr = toString( instance.getCluster().getLocalMember().getInetSocketAddress() );
            for ( int i = 0; i < values.size(); i++ )
            {
                values.set( i, values.get( i ) + " @ " + addr );
            }
        }
        return values;
    }

    private static String toString( final InetSocketAddress address )
    {
        final StringBuilder buf = new StringBuilder();

        final byte[] ip = address.getAddress().getAddress();

        append( buf, ip[0], '.' );
        append( buf, ip[1], '.' );
        append( buf, ip[2], '.' );
        append( buf, ip[3], ':' );

        return buf.append( address.getPort() ).toString();
    }

    private static void append( final StringBuilder buf, final byte value, final char delim )
    {
        buf.append( value & 0xFF ).append( delim );
    }
}

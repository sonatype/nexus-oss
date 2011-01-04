/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.M2TestsuiteEnvironmentBuilder;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public class RepositoryItemUidFactoryTest
    extends AbstractProxyTestEnvironment
{
    protected RepositoryItemUidFactory factory;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );

        return new M2TestsuiteEnvironmentBuilder( ss );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();

        factory = lookup( RepositoryItemUidFactory.class );
    }

    public void testDamianClaim()
        throws Exception
    {
        Repository repository = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );

        RepositoryItemUid uid = factory.createUid( repository, "/some/blammo/poth" );

        uid.lock( Action.read );

        uid.unlock();

        try
        {
            uid.unlock();
        }
        catch ( NullPointerException e )
        {
            e.printStackTrace();
            // damian wins
            fail( "Beer for damian, please!" );
        }
        catch ( IllegalMonitorStateException e )
        {
            e.printStackTrace();
            // cstamas wins
            // fail( "Beer for cstamas, please!" );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( "No beer for anyone" );
        }
    }

    public void testHardAttack()
        throws Exception
    {
        // try with the fixed one
        hardAttack( factory );

        RepositoryItemUidFactory brokenFactory = lookup( RepositoryItemUidFactory.class, "broken" );

        try
        {
            hardAttack( brokenFactory );

            fail( "Test with broken factory should fail!" );
        }
        catch ( AssertionFailedError e )
        {
            // should fail
        }
    }

    public void hardAttack( RepositoryItemUidFactory factory )
        throws Exception
    {
        final int count = 3000;
        final int threadPerPath = 4;

        ExecutorService es = Executors.newFixedThreadPool( 50 );

        Repository repository = getRepositoryRegistry().getRepositoryWithFacet( "repo1", ProxyRepository.class );

        // prepare content, create UID but almost instantly make them GCed
        System.out.println( "Create UIDs to be GCed... " );

        for ( int i = 0; i < count; i++ )
        {
            factory.createUid( repository, "/some/path/" + i );
        }
        System.gc();

        long ser = 1;

        ArrayList<FactoryCreateUidTester> testers =
            new ArrayList<RepositoryItemUidFactoryTest.FactoryCreateUidTester>();

        for ( int i = 0; i < count; i++ )
        {
            RepositoryItemUid uid = factory.createUid( repository, "/some/path/" + i );

            for ( int j = 0; j < threadPerPath; j++ )
            {
                FactoryCreateUidTester tester = new FactoryCreateUidTester( ser++, factory, uid );

                es.execute( tester );

                testers.add( tester );
            }
        }

        Thread.sleep( 1000 );

        // and finally, compare
        // all elements under same key should be equal!
        System.out.println();
        System.out.println( "Waiting for all threads to finish... " );

        es.shutdown();

        while ( !es.isTerminated() )
        {
            Thread.sleep( 1000 );
        }

        System.out.println( "Checking results... " );

        Multimap<String, RepositoryItemUid> uidMap = Multimaps.newArrayListMultimap();

        for ( FactoryCreateUidTester tester : testers )
        {
            uidMap.putAll( tester.getUidMap() );
        }

        for ( String key : uidMap.keySet() )
        {
            Collection<RepositoryItemUid> uids = uidMap.get( key );

            Assert.assertEquals( "There should be as many UIDs for \"" + uids.iterator().next()
                + "\" as many thread getting them!", threadPerPath, uids.size() );

            RepositoryItemUid firstUid = uids.iterator().next();

            for ( RepositoryItemUid uid : uids )
            {
                Assert.assertEquals( "Have to have same instance!",
                    ( (DefaultRepositoryItemUid) firstUid ).toDebugString(),
                    ( (DefaultRepositoryItemUid) uid ).toDebugString() );
            }
        }
    }

    public static final class FactoryCreateUidTester
        implements Runnable
    {
        private final long serial;

        private final RepositoryItemUidFactory factory;

        private final Repository repository;

        private final String path;

        private final Multimap<String, RepositoryItemUid> uidMap;

        public FactoryCreateUidTester( long serial, RepositoryItemUidFactory factory, RepositoryItemUid uid )
        {
            this.serial = serial;

            this.factory = factory;

            this.repository = uid.getRepository();

            this.path = uid.getPath();

            this.uidMap = Multimaps.newArrayListMultimap();
        }

        public Multimap<String, RepositoryItemUid> getUidMap()
        {
            return uidMap;
        }

        public void run()
        {
            if ( serial % 100 == 0 )
            {
                System.out.print( "." );

                System.gc();
            }

            RepositoryItemUid uid = factory.createUid( repository, path );

            // need to sync here, since all thread access this same multimap
            uidMap.put( uid.getPath(), uid );
        }
    }
}

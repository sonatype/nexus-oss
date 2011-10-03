/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4529;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * See NEXUS-4529: This IT bombards Nexus with rest requests to create and drop repositories.
 * 
 * @author cstamas
 */
public class Nexus4529HighlyConcurrentRepoAdditionsAndRemovalsIT
    extends AbstractNexusIntegrationTest
{
    protected RepositoryMessageUtil repoUtil;

    @BeforeClass
    public void prepare()
        throws ComponentLookupException
    {
        repoUtil = new RepositoryMessageUtil( this, getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void doTheTest()
        throws Exception
    {
        execute( 100 );
        verify();
    }

    public void execute( int threadCount )
        throws Exception
    {
        ArrayList<RepoAddRemove> runnables = new ArrayList<RepoAddRemove>( threadCount );
        ArrayList<Future<?>> futures = new ArrayList<Future<?>>( threadCount );

        for ( int i = 0; i < threadCount; i++ )
        {
            runnables.add( new RepoAddRemove( repoUtil, String.valueOf( i ) ) );
        }

        final ExecutorService executor = Executors.newFixedThreadPool( threadCount );

        try
        {
            for ( RepoAddRemove r : runnables )
            {
                futures.add( executor.submit( r ) );
            }

            // wait
            Thread.sleep( 10000 );

            for ( RepoAddRemove r : runnables )
            {
                r.stop();
            }

            // wait
            Thread.sleep( 1000 );

            for ( Future<?> f : futures )
            {
                // this will throw exception if thread failed with HTTP 500 for example
                f.get();
            }
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    public void verify()
    {
        // well, nothing here, the original issue NEXUS-4529 mentions nexus replies with HTTP 500
        // and that will make previous execute() fail anyway. Still, maybe some log search for ERROR?

    }

    public static class RepoAddRemove
        implements Callable<Object>
    {
        private final RepositoryMessageUtil repoUtil;

        private final String prefix;

        private boolean running = true;

        public RepoAddRemove( final RepositoryMessageUtil repoUtil, final String prefix )
        {
            this.repoUtil = repoUtil;

            this.prefix = prefix;
        }

        public void stop()
        {
            this.running = false;
        }

        protected void createRepository( final String repoId )
            throws IOException
        {
            RepositoryResource repo = new RepositoryResource();
            repo.setId( repoId );
            repo.setRepoType( "hosted" );
            repo.setName( repoId );
            repo.setProvider( "maven2" );
            repo.setFormat( "maven2" );
            repo.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
            repoUtil.createRepository( repo, false );
        }

        protected void deleteRepository( final String repoId )
            throws IOException
        {
            repoUtil.sendMessage( Method.DELETE, null, repoId );
        }

        protected String getRepoId( final int counter )
        {
            return prefix + "-" + counter;
        }

        @Override
        public Object call()
            throws Exception
        {
            int counter = 1;

            while ( running )
            {
                createRepository( getRepoId( counter ) );

                // wait
                Thread.sleep( 500 );

                deleteRepository( getRepoId( counter ) );

                counter++;
            }

            return null;
        }
    }
}

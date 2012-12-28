/*
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
package org.sonatype.nexus.proxy.storage.local;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.test.NexusTestSupport;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

/**
 * A simple "benchmarking" test that measures performance of LocalStorage. Unlike other tests (see packages below), this
 * test treats LocalRepositoryStorage as black box, and it does not oppose any assumptions against it, and uses only
 * it's public API.
 * <p>
 * This test is ignored by default, as it does not assert any useful, is meaningful to have it run manually only, for
 * reasons described above.
 * 
 * @author cstamas
 */
@Ignore
public class LocalRepositoryStoragePerformanceTest
    extends NexusTestSupport
{
    @Rule
    public TestName testName = new TestName();

    protected ApplicationConfiguration applicationConfiguration;

    protected RepositoryRegistry repositoryRegistry;

    protected Repository testRepository;

    protected LocalRepositoryStorage testLocalStorage;

    protected final int ROUNDS = 100;

    protected final int[] REPETITIONS = { 5, 10, 15, 20, 25, 50, 100, 150 };

    protected Logger LOG = LoggerFactory.getLogger( getClass() );

    @Before
    public void setUpTestSubject()
        throws Exception
    {
        applicationConfiguration = lookup( ApplicationConfiguration.class );
        repositoryRegistry = lookup( RepositoryRegistry.class );
        testRepository = createRepository( "test1" );
        testLocalStorage = testRepository.getLocalStorage();
    }

    protected Repository createRepository( final String id )
        throws Exception
    {
        final M2Repository repo = (M2Repository) lookup( Repository.class, "maven2" );
        final CRepository repoConf = new DefaultCRepository();
        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( id );
        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );
        repoConf.getLocalStorage().setUrl(
            applicationConfiguration.getWorkingDirectory( "proxy/store/" + id ).toURI().toURL().toString() );
        final Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        final M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );
        repo.configure( repoConf );
        applicationConfiguration.getConfigurationModel().addRepository( repoConf );
        repositoryRegistry.addRepository( repo );
        return repo;
    }

    // ==

    public void retrieveOnLocalStorage( final int rounds )
    {
        for ( int i = 0; i < rounds; i++ )
        {
            try
            {
                testLocalStorage.retrieveItem( testRepository, new ResourceStoreRequest( "/a/" + i ) );
            }
            catch ( ItemNotFoundException e )
            {
                // this is what we expect
            }
            catch ( Exception e )
            {
                Throwables.propagate( e );
            }
        }
    }

    public void retrieveOnRepository( final int rounds )
    {
        for ( int i = 0; i < rounds; i++ )
        {
            try
            {
                testRepository.retrieveItem( new ResourceStoreRequest( "/a/" + i ) );
            }
            catch ( ItemNotFoundException e )
            {
                // this is what we expect
            }
            catch ( RepositoryNotAvailableException e )
            {
                // this is what we expect
            }
            catch ( Exception e )
            {
                Throwables.propagate( e );
            }
        }
    }

    public void repeat( final int[] repetitions, final Runnable runnable )
    {
        LOG.info( "{} START", testName.getMethodName() );
        final Stopwatch sw = new Stopwatch();
        sw.start();
        for ( int i = 0; i < repetitions.length; i++ )
        {
            final int repetition = repetitions[i];
            final Stopwatch rsw = new Stopwatch();
            rsw.start();
            for ( int r = 0; r < repetition; r++ )
            {
                runnable.run();
            }
            rsw.stop();
            LOG.info( "Repetitions={}, RepetitionTime={}", repetition, rsw );
        }
        sw.stop();
        LOG.info( "{} END, totalTime={}", testName.getMethodName(), sw );
    }

    // ==

    @Test
    public void measureRetrieveOnLocalStorage()
        throws Exception
    {
        testRepository.setLocalStatus( LocalStatus.IN_SERVICE );
        testRepository.commitChanges();
        repeat( REPETITIONS, new Runnable()
        {
            @Override
            public void run()
            {
                retrieveOnLocalStorage( ROUNDS );
            }
        } );
    }

    @Test
    public void measureRetrieveOnRepository()
        throws Exception
    {
        testRepository.setLocalStatus( LocalStatus.IN_SERVICE );
        testRepository.commitChanges();
        repeat( REPETITIONS, new Runnable()
        {
            @Override
            public void run()
            {
                retrieveOnRepository( ROUNDS );
            }
        } );
    }

    @Test
    public void measureRetrieveOnRepositoryOutOfService()
        throws Exception
    {
        testRepository.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        testRepository.commitChanges();

        repeat( REPETITIONS, new Runnable()
        {
            @Override
            public void run()
            {
                retrieveOnRepository( ROUNDS );
            }
        } );
    }

}

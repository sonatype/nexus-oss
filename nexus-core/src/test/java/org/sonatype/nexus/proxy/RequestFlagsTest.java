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
package org.sonatype.nexus.proxy;

import static org.sonatype.tests.http.server.fluent.Behaviours.content;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.security.guice.SecurityModule;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Proxy;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.Record;

import com.google.inject.Module;

public class RequestFlagsTest
    extends NexusTestSupport
{
    public static final String PATH = "/test.txt";

    public static final String CONTENT = "foobar123";

    @Override
    protected Module[] getTestCustomModules()
    {
        return new Module[] { new SecurityModule() };
    }

    private Server server;

    private Record recordedRequests;

    private LastModifiedSender lastModifiedSender;

    private Nexus nexus;

    private ProxyRepository proxyRepository;

    @Before
    public void prepare()
        throws Exception
    {
        HttpServletResponse resp;

        recordedRequests = new Record();
        // somewhere in near past
        lastModifiedSender =
            new LastModifiedSender( new Date( System.currentTimeMillis() - TimeUnit.DAYS.toMillis( 3 ) ) );

        server =
            Proxy.withPort( 0 ).serve( PATH ).withBehaviours( recordedRequests, lastModifiedSender, content( CONTENT ) ).start();
        nexus = lookup( Nexus.class );
        proxyRepository = createProxyRepository();

        // disable security
        final NexusConfiguration nexusConfiguration = lookup( NexusConfiguration.class );
        nexusConfiguration.setSecurityEnabled( false );
        nexusConfiguration.saveConfiguration();
    }

    @After
    public void cleanup()
        throws Exception
    {
        server.stop();
    }

    protected ProxyRepository createProxyRepository()
        throws Exception
    {
        final RepositoryTemplate template =
            (RepositoryTemplate) nexus.getRepositoryTemplates().getTemplates( Maven2ContentClass.class,
                RepositoryPolicy.RELEASE, MavenProxyRepository.class ).pick();
        final ConfigurableRepository templateConf = template.getConfigurableRepository();
        templateConf.setId( "test" );
        templateConf.setName( "Test" );
        final CRemoteStorage remoteStorageConf = new CRemoteStorage();
        remoteStorageConf.setUrl( "http://localhost:" + server.getPort() );
        template.getCoreConfiguration().getConfiguration( true ).setRemoteStorage( remoteStorageConf );
        final MavenProxyRepository mavenProxyRepository = (MavenProxyRepository) template.create();

        return mavenProxyRepository;
    }

    // ==

    @Test
    public void localOnlyFlagWithEmptyCacheIs404()
        throws Exception
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestLocalOnly( true );
        try
        {
            proxyRepository.retrieveItem( request );
            Assert.fail( "We should get INFEx!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        MatcherAssert.assertThat( recordedRequests.getRequests(), Matchers.empty() );
    }

    @Test
    public void localOnlyFlagWithPrimedCacheIsServed()
        throws Exception
    {
        // prime the cache
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestLocalOnly( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 1 ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void localOnlyFlagWithExpiredCacheIsServed()
        throws Exception
    {
        // prime the cache and make it expired
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
            proxyRepository.expireCaches( new ResourceStoreRequest( "/" ) );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestLocalOnly( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 1 ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    // ==

    @Test
    public void remoteOnlyFlagWithEmptyCacheGoesRemoteAndIsServed()
        throws Exception
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestRemoteOnly( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 1 ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void remoteOnlyFlagWithPrimedCacheGoesRemoteAndIsServed()
        throws Exception
    {
        // prime the cache
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestRemoteOnly( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server!
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 2 ) );
        // BOTH requests were GETs
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void remoteOnlyFlagWithPrimedCacheGoesRemoteAndIsDeletedIfNotFound()
        throws Exception
    {
        // igor: another discrepancy: with remoteOnly Nexus _deletes_
        // local cache content if it was deleted remotely, unlike in
        // "normal" case (this was true since beginning)

        // prime the cache
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
        }

        // recreate server to report 404 for path
        final int port = server.getPort();
        server.stop();
        server =
            Proxy.withPort( port ).serve( PATH ).withBehaviours( recordedRequests, Behaviours.error( 404, "Not found" ) ).start();

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestRemoteOnly( true );
        try
        {
            final StorageItem item = proxyRepository.retrieveItem( request );
            Assert.fail( "We should get INFEx!" );
        }
        catch ( ItemNotFoundException e )
        {
            // good
        }

        // BOTH requests will go to remote server!
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 2 ) );
        // BOTH requests were GETs
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "GET" ) );

        // cache got removed coz of 404!
        MatcherAssert.assertThat(
            proxyRepository.getLocalStorage().containsItem( proxyRepository, new ResourceStoreRequest( PATH ) ),
            Matchers.is( false ) );
    }

    @Test
    public void remotelOnlyFlagWithExpiredCacheGoesRemoteAndIsServed()
        throws Exception
    {
        // prime the cache and make it expired
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
            proxyRepository.expireCaches( new ResourceStoreRequest( "/" ) );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestRemoteOnly( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server!
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 2 ) );
        // BOTH requests were GETs
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    // ==

    @Test
    public void asExpireFlagWithEmptyCacheGoesRemoteAndIsServed()
        throws Exception
    {
        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestAsExpired( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 1 ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void asExpiredFlagWithPrimedCacheGoesRemoteAndIsServed()
        throws Exception
    {
        // prime the cache
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestAsExpired( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 2 ) );
        // But, requests are GET and HEAD (1st is for "prime", 2nd is checking for remote)
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "HEAD" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 2 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void asExpiredFlagWithPrimedCacheGoesRemoteAndIsServedWithRemoteNewer()
        throws Exception
    {
        // prime the cache
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
        }
        
        lastModifiedSender.setLastModified( new Date() );

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestAsExpired( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server (but 2nd will do HEAD only request)!
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 3 ) );
        // But, requests are GET, HEAD and GET (1st is for "prime", 2nd is checking for remote, and 3rd one actually
        // GETs it
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "HEAD" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 2 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void asExpiredFlagWithExpiredCacheGoesRemoteAndIsServed()
        throws Exception
    {
        // prime the cache and make it expired
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
            proxyRepository.expireCaches( new ResourceStoreRequest( "/" ) );
        }

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestAsExpired( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 2 ) );
        // But, requests are GET and HEAD (1st is for "prime", 2nd is checking for remote)
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "HEAD" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    @Test
    public void asExpiredFlagWithExpiredCacheGoesRemoteAndIsServedWithRemoteNewer()
        throws Exception
    {
        // prime the cache and make it expired
        {
            final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
            proxyRepository.retrieveItem( request );
            proxyRepository.expireCaches( new ResourceStoreRequest( "/" ) );
        }
        
        lastModifiedSender.setLastModified( new Date() );

        final ResourceStoreRequest request = new ResourceStoreRequest( PATH );
        request.setRequestAsExpired( true );
        final StorageItem item = proxyRepository.retrieveItem( request );

        // BOTH requests will go to remote server (but 2nd will do TWO HTTP requests)!
        MatcherAssert.assertThat( recordedRequests.getRequests().size(), Matchers.equalTo( 3 ) );
        // But, requests are GET, HEAD and GET (1st is for "prime", 2nd is checking for remote, and 3rd one actually
        // GETs it
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 0 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 1 ), Matchers.startsWith( "HEAD" ) );
        MatcherAssert.assertThat( recordedRequests.getRequests().get( 2 ), Matchers.startsWith( "GET" ) );
        MatcherAssert.assertThat( item, Matchers.instanceOf( StorageFileItem.class ) );

        final String content = IOUtils.toString( ( (StorageFileItem) item ).getContentLocator().getContent() );
        MatcherAssert.assertThat( content, Matchers.equalTo( CONTENT ) );
    }

    // ==

    public class LastModifiedSender
        implements Behaviour
    {
        private Date lastModified;

        public LastModifiedSender( final Date date )
        {
            setLastModified( date );
        }

        public void setLastModified( final Date when )
        {
            lastModified = when;
        }

        @Override
        public boolean execute( HttpServletRequest request, HttpServletResponse response, Map<Object, Object> ctx )
            throws Exception
        {
            response.setDateHeader( "last-modified", lastModified.getTime() );
            return true;
        }
    }
}

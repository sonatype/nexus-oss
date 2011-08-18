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
package org.sonatype.nexus.plugins.rrb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.mirror.DownloadMirrors;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.DefaultRemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.DefaultRemoteProxySettings;
import org.sonatype.nexus.proxy.repository.ItemContentValidator;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ProxySelector;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryStatusCheckMode;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

import com.ning.http.client.AsyncHttpClient;

/**
 * In this test we use example repo files that placed in the test resource catalogue To access these files locally via
 * MavenRepositoryReader that requires the http-protocol we start a Jetty server
 * 
 * @author bjorne
 */
public class MavenRepositoryReaderTest
{
    MavenRepositoryReader reader; // The "class under test"

    Server server; // An embedded Jetty server

    String localUrl = "http://local"; // This URL doesn't matter for the tests

    String nameOfConnector; // This is the host:portnumber of the Jetty connector

    @Before
    public void setUp()
        throws Exception
    {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        reader = new MavenRepositoryReader( asyncHttpClient );

        // Create a Jetty server with a handler that returns the content of the
        // given target (i.e. an emulated html, S3Repo, etc, file from the test
        // resources)
        Handler handler = new AbstractHandler()
        {

            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                String path = target;
                if ( path.endsWith( "/" ) && StringUtils.isNotEmpty( request.getParameter( "prefix" ) ) )
                {
                    String prefix = request.getParameter( "prefix" );
                    path = path + prefix.replaceAll( "/", "-" );
                }
                else if ( target.endsWith( "/" ) )
                {
                    // might need welcome pages later.
                    path += "root";
                }

                response.setStatus( HttpServletResponse.SC_OK );
                InputStream stream = this.getClass().getResourceAsStream( path );

                // added to make old tests work
                // we need to fall back to the file name that matches
                if ( stream == null && path.endsWith( "root" ) )
                {
                    path = target;
                    stream = this.getClass().getResourceAsStream( path );
                }

                if ( stream == null )
                {
                    System.out.println( "Error handling: " + path );
                }

                StringBuilder result = new StringBuilder();
                BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );

                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    result.append( line ).append( System.getProperty( "line.separator" ) );
                }
                response.getWriter().println( result.toString() );
                ( (Request) request ).setHandled( true );
            }
        };

        server = new Server( 0 ); // We choose an arbitrary server port
        server.setHandler( handler ); // Assign the handler of incoming requests
        server.start();

        // After starting we must find out the host:port, so we know how to
        // connect to the server in the tests
        for ( Connector connector : server.getConnectors() )
        {
            nameOfConnector = connector.getName();
            break; // We only need one connector name (and there should only be
            // one...)
        }

    }

    @After
    public void shutDown()
        throws Exception
    {
        server.stop();
    }

    /**
     * First some tests of architypical test repos
     */

    @Test( timeout = 5000 )
    public void testReadHtml()
    {
        List<RepositoryDirectory> result =
            reader.extract( "htmlExample", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 7, result.size() );
    }

    @Test( timeout = 5000 )
    public void testReadS3()
    {
        List<RepositoryDirectory> result =
            reader.extract( "s3Example", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 13, result.size() );
    }

    @Test( timeout = 5000 )
    public void testReadProtectedS3()
    {
        // Fetched from URI http://coova-dev.s3.amazonaws.com/mvn/
        // This S3 repo does _work_ (with maven and/or nexus proxying it), but it's setup (perms) does not allow
        // "public browsing".
        List<RepositoryDirectory> result =
            reader.extract( "s3Example-foreign", localUrl,
                new FakeProxyRepo( "http://coova-dev.s3.amazonaws.com/mvn/" ), "test" );
        assertEquals( 0, result.size() );
    }

    @Test( timeout = 5000 )
    public void testReadArtifactory()
    {
        // In this test the format of the local URL is important
        localUrl =
            "http://localhost:8081/nexus/service/local/repositories/ArtyJavaNet/remotebrowser/http://repo.jfrog.org/artifactory/java.net";
        List<RepositoryDirectory> result =
            reader.extract( "Artifactory.java.net.htm", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 30, result.size() );
    }

    /**
     * Below follows a set of tests of some typical existing repos. The respectively repo's top level is stored as a
     * file in the ordinary test resource catalog. Each file has a name indicating the repo it is taken from and an
     * extension with the date it was downloaded in the format YYYYMMDD.
     */

    @Test( timeout = 5000 )
    public void testAmazon_20100118()
    {
        // Fetched from URI http://s3.amazonaws.com/maven.springframework.org
        List<RepositoryDirectory> result =
            reader.extract( "/", localUrl, new FakeProxyRepo( getRemoteUrl() + "Amazon_20100118" ), "test" );
        assertEquals( 997, result.size() );

        for ( RepositoryDirectory repositoryDirectory : result )
        {
            assertFalse( repositoryDirectory.getRelativePath().contains( "prefix" ) );
            assertFalse( repositoryDirectory.getResourceURI().contains( "prefix" ) );
        }
    }

    @Test( timeout = 5000 )
    public void testAmazon_20110112_slashCom()
    {
        // Fetched from URI http://repository.springsource.com/?prifix=maven/bundles/release&delimiter=/
        // and http://repository.springsource.com/maven/bundles/release/com
        List<RepositoryDirectory> result =
            reader.extract( "/com/", localUrl, new FakeProxyRepo( getRemoteUrl()
                + "Amazon_20110112/maven/bundles/release" ), "test" );
        assertEquals( "Result: " + result, 1, result.size() );

        RepositoryDirectory repositoryDirectory1 = result.get( 0 );
        Assert.assertFalse( repositoryDirectory1.isLeaf() );
        Assert.assertEquals( localUrl + "/com/springsource/", repositoryDirectory1.getResourceURI() );
        Assert.assertEquals( "/com/springsource/", repositoryDirectory1.getRelativePath() );
    }

    @Test
    // ( timeout = 5000 )
    public void testAmazon_20110112_slashRoot()
    {
        // Fetched from URI http://repository.springsource.com/?prifix=maven/bundles/release&delimiter=/
        // and http://repository.springsource.com/maven/bundles/release/
        List<RepositoryDirectory> result =
            reader.extract( "/", localUrl,
                new FakeProxyRepo( getRemoteUrl() + "Amazon_20110112/maven/bundles/release" ), "test" );
        assertEquals( "Result: " + result, 2, result.size() );

        RepositoryDirectory repositoryDirectory1 = result.get( 0 );
        Assert.assertFalse( repositoryDirectory1.isLeaf() );
        Assert.assertEquals( localUrl + "/com/", repositoryDirectory1.getResourceURI() );
        Assert.assertEquals( "/com/", repositoryDirectory1.getRelativePath() );

        RepositoryDirectory repositoryDirectory2 = result.get( 1 );
        Assert.assertFalse( repositoryDirectory2.isLeaf() );
        Assert.assertEquals( localUrl + "/org/", repositoryDirectory2.getResourceURI() );
        Assert.assertEquals( "/org/", repositoryDirectory2.getRelativePath() );
    }

    @Test( timeout = 5000 )
    public void testApache_Snapshots()
    {
        // Fetched from URI http://repository.apache.org/snapshots
        List<RepositoryDirectory> result =
            reader.extract( "Apache_Snapshots_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 9, result.size() );
    }

    @Test( timeout = 5000 )
    public void testCodehaus_Snapshots()
    {
        // Fetched from URI http://snapshots.repository.codehaus.org/
        List<RepositoryDirectory> result =
            reader.extract( "Codehaus_Snapshots_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 3, result.size() );
    }

    @Test( timeout = 5000 )
    public void testGoogle_Caja()
    {
        // Fetched from URI http://google-caja.googlecode.com/svn/maven
        List<RepositoryDirectory> result =
            reader.extract( "Google_Caja_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 3, result.size() );
    }

    @Test( timeout = 5000 )
    public void testGoogle_Oauth()
    {
        // Fetched from URI http://oauth.googlecode.com/svn/code/maven
        List<RepositoryDirectory> result =
            reader.extract( "Google_Oauth_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 4, result.size() );
    }

    @Test( timeout = 5000 )
    public void testJBoss_Maven_Release_Repository()
    {
        // Fetched from URI http://repository.jboss.org/maven2/
        List<RepositoryDirectory> result =
            reader.extract( "JBoss_Maven_Release_Repository_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ),
                "test" );
        assertEquals( 201, result.size() );
    }

    @Test( timeout = 5000 )
    public void testMaven_Central()
    {
        // Fetched from URI http://repo1.maven.org/maven2
        List<RepositoryDirectory> result =
            reader.extract( "Maven_Central_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 647, result.size() );
    }

    @Test( timeout = 5000 )
    public void testNexus_Repository_Manager()
    {
        // Fetched from URI http://repository.sonatype.org/content/groups/forge
        List<RepositoryDirectory> result =
            reader.extract( "Nexus_Repository_Manager_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 173, result.size() );
    }

    @Test( timeout = 5000 )
    public void testEviwares_Maven_repo()
    {
        // Fetched from URI http://www.eviware.com/repository/maven2/
        List<RepositoryDirectory> result =
            reader.extract( "Eviwares_Maven_repo_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 67, result.size() );
    }

    @Test( timeout = 5000 )
    public void testjavaNet_repo()
    {
        // Fetched from URI http://download.java.net/maven/1/
        List<RepositoryDirectory> result =
            reader.extract( "java.net_repo_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 94, result.size() );
    }

    @Test( timeout = 5000 )
    public void testCodehaus()
    {
        // Fetched from URI http://repository.codehaus.org/
        List<RepositoryDirectory> result =
            reader.extract( "Codehaus_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 5, result.size() );
    }

    @Test( timeout = 5000 )
    public void testjavaNet2()
    {
        // Fetched from URI http://download.java.net/maven/2/
        List<RepositoryDirectory> result =
            reader.extract( "java.net2_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 57, result.size() );
    }

    @Test( timeout = 5000 )
    public void testOpenIonaCom_Releases()
    {
        // Fetched from URI http://repo.open.iona.com/maven2/
        List<RepositoryDirectory> result =
            reader.extract( "Open.iona.com_Releases_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 8, result.size() );
    }

    /*
     * @Test(timeout = 5000) public void testterracotta() { // Fetched from URI http://download.terracotta.org/maven2/
     * List<RepositoryDirectory> result = reader .extract(getURLForTestRepoResource("terracotta_20100118"), localUrl,
     * null, "test"); assertEquals(-1, result.size()); }
     */

    @Test( timeout = 5000 )
    public void testSpringsource()
    {
        // Fetched from URI http://repository.springsource.com/
        List<RepositoryDirectory> result =
            reader.extract( "Springsource_20100118", localUrl, new FakeProxyRepo( getRemoteUrl() ), "test" );
        assertEquals( 995, result.size() );
    }

    /**
     * Auxiliary methods
     */
    private String getURLForTestRepoResource( String resourceName )
    {
        return this.getRemoteUrl() + resourceName;
    }

    private String getRemoteUrl()
    {
        return "http://" + nameOfConnector + "/";
    }

    static class FakeProxyRepo
        implements ProxyRepository
    {
        private String remoteUrl;

        public FakeProxyRepo( String remoteUrl )
        {
            this.remoteUrl = remoteUrl;
        }

        public String getProviderRole()
        {

            return null;
        }

        public String getProviderHint()
        {

            return null;
        }

        public String getId()
        {

            return null;
        }

        public void setId( String id )
        {

        }

        public String getName()
        {

            return null;
        }

        public void setName( String name )
        {

        }

        public String getPathPrefix()
        {

            return null;
        }

        public void setPathPrefix( String prefix )
        {

        }

        public RepositoryKind getRepositoryKind()
        {

            return null;
        }

        public ContentClass getRepositoryContentClass()
        {

            return null;
        }

        public RepositoryTaskFilter getRepositoryTaskFilter()
        {

            return null;
        }

        public TargetSet getTargetsForRequest( ResourceStoreRequest request )
        {

            return null;
        }

        public boolean hasAnyTargetsForRequest( ResourceStoreRequest request )
        {

            return false;
        }

        public RepositoryItemUid createUid( String path )
        {

            return null;
        }

        public RepositoryItemUidAttributeManager getRepositoryItemUidAttributeManager()
        {

            return null;
        }

        public Action getResultingActionOnWrite( ResourceStoreRequest rsr )
        {

            return null;
        }

        public boolean isCompatible( Repository repository )
        {

            return false;
        }

        public <F> F adaptToFacet( Class<F> t )
        {

            return null;
        }

        public int getNotFoundCacheTimeToLive()
        {

            return 0;
        }

        public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
        {

        }

        public PathCache getNotFoundCache()
        {

            return null;
        }

        public void setNotFoundCache( PathCache notFoundcache )
        {

        }

        public void maintainNotFoundCache( ResourceStoreRequest request )
            throws ItemNotFoundException
        {

        }

        public void addToNotFoundCache( String path )
        {

        }

        public void removeFromNotFoundCache( String path )
        {

        }

        public void addToNotFoundCache( ResourceStoreRequest request )
        {

        }

        public void removeFromNotFoundCache( ResourceStoreRequest request )
        {

        }

        public boolean isNotFoundCacheActive()
        {

            return false;
        }

        public void setNotFoundCacheActive( boolean notFoundCacheActive )
        {

        }

        public AttributesHandler getAttributesHandler()
        {

            return null;
        }

        public void setAttributesHandler( AttributesHandler attributesHandler )
        {

        }

        public String getLocalUrl()
        {

            return null;
        }

        public void setLocalUrl( String url )
            throws StorageException
        {

        }

        public LocalStatus getLocalStatus()
        {

            return null;
        }

        public void setLocalStatus( LocalStatus val )
        {

        }

        public LocalStorageContext getLocalStorageContext()
        {

            return null;
        }

        public LocalRepositoryStorage getLocalStorage()
        {

            return null;
        }

        public void setLocalStorage( LocalRepositoryStorage storage )
        {

        }

        public PublishedMirrors getPublishedMirrors()
        {

            return null;
        }

        public Map<String, RequestProcessor> getRequestProcessors()
        {

            return null;
        }

        public boolean isUserManaged()
        {

            return false;
        }

        public void setUserManaged( boolean val )
        {

        }

        public boolean isExposed()
        {

            return false;
        }

        public void setExposed( boolean val )
        {

        }

        public boolean isBrowseable()
        {

            return false;
        }

        public void setBrowseable( boolean val )
        {

        }

        public RepositoryWritePolicy getWritePolicy()
        {

            return null;
        }

        public void setWritePolicy( RepositoryWritePolicy writePolicy )
        {

        }

        public boolean isIndexable()
        {

            return false;
        }

        public void setIndexable( boolean val )
        {

        }

        public boolean isSearchable()
        {

            return false;
        }

        public void setSearchable( boolean val )
        {

        }

        public void expireCaches( ResourceStoreRequest request )
        {

        }

        public void expireNotFoundCaches( ResourceStoreRequest request )
        {

        }

        public Collection<String> evictUnusedItems( ResourceStoreRequest request, long timestamp )
        {

            return null;
        }

        public boolean recreateAttributes( ResourceStoreRequest request, Map<String, String> initialData )
        {

            return false;
        }

        public AccessManager getAccessManager()
        {

            return null;
        }

        public void setAccessManager( AccessManager accessManager )
        {

        }

        public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
            throws IllegalOperationException, ItemNotFoundException, StorageException
        {

            return null;
        }

        public void copyItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
            StorageException
        {

        }

        public void moveItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
            StorageException
        {

        }

        public void deleteItem( boolean fromTask, ResourceStoreRequest request )
            throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException,
            StorageException
        {

        }

        public Collection<StorageItem> list( boolean fromTask, ResourceStoreRequest request )
            throws IllegalOperationException, ItemNotFoundException, StorageException
        {

            return null;
        }

        public void storeItem( boolean fromTask, StorageItem item )
            throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
        {

        }

        public Collection<StorageItem> list( boolean fromTask, StorageCollectionItem item )
            throws IllegalOperationException, ItemNotFoundException, StorageException
        {

            return null;
        }

        public StorageItem retrieveItem( ResourceStoreRequest request )
            throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
        {

            return null;
        }

        public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
            StorageException, AccessDeniedException
        {

        }

        public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
            throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
            StorageException, AccessDeniedException
        {

        }

        public void deleteItem( ResourceStoreRequest request )
            throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
            StorageException, AccessDeniedException
        {

        }

        public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
            throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
            StorageException, AccessDeniedException
        {

        }

        public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
            throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
            StorageException, AccessDeniedException
        {

        }

        public Collection<StorageItem> list( ResourceStoreRequest request )
            throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
        {

            return null;
        }

        public CoreConfiguration getCurrentCoreConfiguration()
        {

            return null;
        }

        public void configure( Object config )
            throws ConfigurationException
        {

        }

        public boolean isDirty()
        {

            return false;
        }

        public boolean commitChanges()
            throws ConfigurationException
        {

            return false;
        }

        public boolean rollbackChanges()
        {

            return false;
        }

        public RemoteStatus getRemoteStatus( ResourceStoreRequest request, boolean forceCheck )
        {

            return null;
        }

        public Thread getRepositoryStatusCheckerThread()
        {

            return null;
        }

        public void setRepositoryStatusCheckerThread( Thread thread )
        {

        }

        public long getCurrentRemoteStatusRetainTime()
        {

            return 0;
        }

        public long getNextRemoteStatusRetainTime()
        {

            return 0;
        }

        public ProxyMode getProxyMode()
        {

            return null;
        }

        public void setProxyMode( ProxyMode val )
        {

        }

        public int getItemMaxAge()
        {

            return 0;
        }

        public void setItemMaxAge( int itemMaxAge )
        {

        }

        public boolean isFileTypeValidation()
        {

            return false;
        }

        public void setFileTypeValidation( boolean doValidate )
        {

        }

        public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
        {

            return null;
        }

        public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
        {

        }

        public boolean isAutoBlockActive()
        {

            return false;
        }

        public void setAutoBlockActive( boolean val )
        {

        }

        public String getRemoteUrl()
        {
            return remoteUrl;
        }

        public void setRemoteUrl( String url )
            throws RemoteStorageException
        {

        }

        public DownloadMirrors getDownloadMirrors()
        {

            return null;
        }

        public RemoteConnectionSettings getRemoteConnectionSettings()
        {

            return null;
        }

        public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
        {

        }

        public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
        {

            return null;
        }

        public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
        {

        }

        public RemoteProxySettings getRemoteProxySettings()
        {

            return null;
        }

        public void setRemoteProxySettings( RemoteProxySettings settings )
        {

        }

        public ProxySelector getProxySelector()
        {

            return null;
        }

        public void setProxySelector( ProxySelector proxySelector )
        {

        }

        public boolean isItemAgingActive()
        {

            return false;
        }

        public void setItemAgingActive( boolean value )
        {

        }

        public RemoteStorageContext getRemoteStorageContext()
        {
            DefaultRemoteStorageContext rsc = new DefaultRemoteStorageContext( null );
            rsc.setRemoteProxySettings( new DefaultRemoteProxySettings() );
            rsc.setRemoteConnectionSettings( new DefaultRemoteConnectionSettings() );
            return rsc;
        }

        public RemoteRepositoryStorage getRemoteStorage()
        {

            return null;
        }

        public void setRemoteStorage( RemoteRepositoryStorage storage )
        {

        }

        public Map<String, ItemContentValidator> getItemContentValidators()
        {

            return null;
        }

        public AbstractStorageItem doCacheItem( AbstractStorageItem item )
            throws LocalStorageException
        {

            return null;
        }

    }

}

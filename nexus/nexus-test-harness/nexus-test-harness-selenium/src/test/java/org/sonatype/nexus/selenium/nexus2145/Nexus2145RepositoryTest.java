package org.sonatype.nexus.selenium.nexus2145;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;

public class Nexus2145RepositoryTest
    extends SeleniumTest
{

    private static final String INCREMENTAL_REINDEX_URI = "/data_incremental_index/{domain}/{target}/content";

    private static final String REBUILD_URI = "/metadata/{domain}/{target}/content";

    private static final String REINDEX_URI = "/data_index/{domain}/{target}/content";

    private static final String EXPIRE_CACHE_URI = "/data_cache/{domain}/{target}/content";

    private Nexus nexus;

    private M2Repository proxyRepo;

    private M2Repository hostedRepo;

    @Test
    public void errorMessagesHosted()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RepositoriesConfigurationForm newHosted = main.openRepositories().addHostedRepo().save();

        Assert.assertTrue( "Task type is a required field",
                           newHosted.getIdField().hasErrorText( "This field is required" ) );
        Assert.assertTrue( "Name is a required field", newHosted.getName().hasErrorText( "This field is required" ) );
    }

    @Test
    public void crudHosted()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-repo";
        String name = "Selenium repository";
        repositories.addHostedRepo().populate( repoId, name ).save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "hosted", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();

        String newName = "new selenium repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.HOSTED ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.HOSTED );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

    @Test
    public void crudVirtual()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-virtual-repo";
        String name = "Selenium Virtual repository";
        RepositoriesConfigurationForm virtualRepo =
            repositories.addVirtualRepo().populateVirtual( repoId, name, "m2-m1-shadow", "releases" );
        virtualRepo.save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "virtual", config.getType().getValue() );
        Assert.assertEquals( "maven1", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();

        String newName = "new selenium virtual repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.VIRTUAL ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.VIRTUAL );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

    @Test
    public void crudProxy()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // Create
        RepositoriesTab repositories = main.openRepositories();
        String repoId = "selenium-proxy-repo";
        String name = "Selenium Proxy repository";
        repositories.addProxyRepo().populateProxy( repoId, name,
                                                   "http://repository.sonatype.org/content/groups/public/" ).save();
        repositories.refresh();

        // read
        RepositoriesConfigurationForm config =
            (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();

        Assert.assertEquals( repoId, config.getIdField().getValue() );
        Assert.assertEquals( name, config.getName().getValue() );
        Assert.assertEquals( "proxy", config.getType().getValue() );
        Assert.assertEquals( "maven2", config.getFormat().getValue() );
        repositories.refresh();

        // update
        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();

        String newName = "new selenium proxy repo name";
        config.getName().type( newName );
        config.save();

        repositories.refresh();

        config = (RepositoriesConfigurationForm) repositories.select( repoId, RepoKind.PROXY ).selectConfiguration();
        Assert.assertEquals( newName, config.getName().getValue() );

        repositories.refresh();

        // delete
        repositories.select( repoId, RepoKind.PROXY );
        repositories.delete().clickYes();
        repositories.refresh();

        Assert.assertFalse( repositories.contains( repoId ) );
    }

    @Before
    public void createRepo()
        throws Exception
    {
        nexus = lookup( Nexus.class );
        CRepository cRepo = new CRepository();
        cRepo.setId( "nexus2145" );
        cRepo.setName( "nexus2145" );
        cRepo.setProviderRole( Repository.class.getName() );
        cRepo.setProviderHint( "maven2" );

        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.name() );

        Xpp3Dom ex = new Xpp3Dom( "externalConfiguration" );
        cRepo.setExternalConfiguration( ex );
        M2RepositoryConfiguration exConf = new M2RepositoryConfiguration( ex );
        exConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exConf.applyChanges();

        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setUrl( cRepo.defaultLocalStorageUrl );
        cRepo.getLocalStorage().setProvider( "file" );

        cRepo.setRemoteStorage( new CRemoteStorage() );
        cRepo.getRemoteStorage().setProvider( CommonsHttpClientRemoteStorage.PROVIDER_STRING );
        cRepo.getRemoteStorage().setUrl( "http://some-remote-repository/repo-root" );
        cRepo.setExposed( true );
        cRepo.setUserManaged( true );
        cRepo.setIndexable( true );
        cRepo.setBrowseable( true );

        proxyRepo = (M2Repository) nexus.createRepository( cRepo );

        cRepo = new CRepository();
        cRepo.setId( "hosted-nexus2145" );
        cRepo.setName( "hosted-nexus2145" );
        cRepo.setProviderRole( Repository.class.getName() );
        cRepo.setProviderHint( "maven2" );

        cRepo.setLocalStatus( LocalStatus.IN_SERVICE.name() );

        ex = new Xpp3Dom( "externalConfiguration" );
        cRepo.setExternalConfiguration( ex );
        exConf = new M2RepositoryConfiguration( ex );
        exConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exConf.applyChanges();

        cRepo.setLocalStorage( new CLocalStorage() );
        cRepo.getLocalStorage().setUrl( cRepo.defaultLocalStorageUrl );
        cRepo.getLocalStorage().setProvider( "file" );

        cRepo.setExposed( true );
        cRepo.setUserManaged( true );
        cRepo.setIndexable( true );
        cRepo.setBrowseable( true );

        hostedRepo = (M2Repository) nexus.createRepository( cRepo );
    }

    @After
    public void deleteRepo()
        throws Exception
    {
        nexus.deleteRepository( proxyRepo.getId() );
        nexus.deleteRepository( hostedRepo.getId() );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void contextMenu()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RepositoriesTab repositories = main.openRepositories();

        // expire cache
        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.SUCCESS_OK, new NFCResourceResponse() ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // rebuild metadata
        MockHelper.expect( REBUILD_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( REBUILD_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // reindex
        MockHelper.expect( REINDEX_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuReindex( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( REINDEX_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuReindex( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // incremental reindex
        MockHelper.expect( INCREMENTAL_REINDEX_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuIncrementalReindex( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( INCREMENTAL_REINDEX_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuIncrementalReindex( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // put out of service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        Assert.assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.OUT_OF_SERVICE ) );
        // check on UI
        Assert.assertThat( repositories.getStatus( hostedRepo.getId() ), equalTo( "Out of Service" ) );

        // back to service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST,
                                                                                    null ) );
        repositories.contextMenuPutInService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutInService( hostedRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        Assert.assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.IN_SERVICE ) );
        // check on UI
        Assert.assertThat( repositories.getStatus( hostedRepo.getId() ), equalTo( "In Service" ) );

        // put out of service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        Assert.assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.BLOCKED_MANUAL ) );
        // check on UI
        Assert.assertThat( repositories.getStatus( proxyRepo.getId() ),
                           anyOf( equalTo( "In Service - Remote Manually Blocked and Available" ),
                                  equalTo( "In Service - Remote Manually Blocked and Unavailable" ) ) );

        // back to service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        Assert.assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.ALLOW ) );
        // check on UI
        Assert.assertThat( repositories.getStatus( proxyRepo.getId() ),
                           anyOf( equalTo( "In Service" ), equalTo( "In Service - <I>checking remote...</I>" ) ) );
    }
}

package org.sonatype.nexus.selenium.nexus2145;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.hamcrest.CoreMatchers;
import org.restlet.data.Status;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.sonatype.nexus.mock.pages.RepositoriesTab;
import org.sonatype.nexus.mock.pages.RepositoriesEditTabs.RepoKind;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.NFCResourceResponse;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2ProxyRepositoryTemplate;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Component( role = Nexus2145RepositoryTest.class )
public class Nexus2145RepositoryTest
    extends SeleniumTest
{

    private static final String INCREMENTAL_REINDEX_URI = "/data_incremental_index/{domain}/{target}/content";

    private static final String REBUILD_URI = "/metadata/{domain}/{target}/content";

    private static final String REINDEX_URI = "/data_index/{domain}/{target}/content";

    private static final String EXPIRE_CACHE_URI = "/data_cache/{domain}/{target}/content";

    @Requirement
    private Nexus nexus;

    @Requirement
    private RepositoryRegistry config;

    private M2Repository proxyRepo;

    private M2Repository hostedRepo;

    @Test
    public void errorMessagesHosted()
        throws InterruptedException
    {
        doLogin();

        RepositoriesConfigurationForm newHosted = main.openRepositories().addHostedRepo().save();

        assertTrue( "Task type is a required field", newHosted.getIdField().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newHosted.getName().hasErrorText( "This field is required" ) );
    }

    @Test
    public void crudHosted()
        throws InterruptedException
    {
        doLogin();

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
        doLogin();

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
        doLogin();

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

    @BeforeClass
    public void createRepo()
        throws Exception
    {
        Maven2ProxyRepositoryTemplate template =
            (Maven2ProxyRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2ProxyRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        template.getConfigurableRepository().setId( "nexus2145" );
        template.getConfigurableRepository().setName( "nexus2145" );

        template.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        template.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        template.getConfigurableRepository().setExposed( true );
        template.getConfigurableRepository().setUserManaged( true );
        template.getConfigurableRepository().setIndexable( true );
        template.getConfigurableRepository().setBrowseable( true );

        proxyRepo = (M2Repository) template.create();

        Maven2HostedRepositoryTemplate hostedTemplate =
            (Maven2HostedRepositoryTemplate) nexus.getRepositoryTemplates()
                .getTemplates( Maven2ProxyRepositoryTemplate.class, RepositoryPolicy.RELEASE ).pick();

        hostedTemplate.getConfigurableRepository().setId( "hosted-nexus2145" );
        hostedTemplate.getConfigurableRepository().setName( "hosted-nexus2145" );

        hostedTemplate.getConfigurableRepository().setLocalStatus( LocalStatus.IN_SERVICE );

        hostedTemplate.setRepositoryPolicy( RepositoryPolicy.RELEASE );

        hostedTemplate.getConfigurableRepository().setExposed( true );
        hostedTemplate.getConfigurableRepository().setUserManaged( true );
        hostedTemplate.getConfigurableRepository().setIndexable( true );
        hostedTemplate.getConfigurableRepository().setBrowseable( true );

        hostedRepo = (M2Repository) hostedTemplate.create();
    }

    @AfterClass
    public void deleteRepo()
        throws Exception
    {
        nexus.deleteRepository( proxyRepo.getId() );
        nexus.deleteRepository( hostedRepo.getId() );
    }

    @Test
    public void contextMenuExpireCache()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // expire cache
        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.SUCCESS_OK, new NFCResourceResponse() ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( EXPIRE_CACHE_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuExpireCache( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

    private RepositoriesTab startContextMenuTest()
        throws NoSuchRepositoryException
    {
        assertThat( config.getRepository( hostedRepo.getId() ), CoreMatchers.notNullValue() );
        assertThat( config.getRepository( proxyRepo.getId() ), CoreMatchers.notNullValue() );

        doLogin();

        RepositoriesTab repositories = main.openRepositories();
        return repositories;
    }

    @Test
    public void contextMenuRebuildMetadata()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // rebuild metadata
        MockHelper.expect( REBUILD_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( REBUILD_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuRebuildMetadata( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

    @Test
    public void contextMenuIndex()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // reindex
        MockHelper.expect( REINDEX_URI, new MockResponse( Status.SUCCESS_OK, null ) );
        repositories.contextMenuReindex( hostedRepo.getId() );
        new Window( selenium ).waitFor();

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
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( INCREMENTAL_REINDEX_URI, new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST, null ) );
        repositories.contextMenuIncrementalReindex( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

    @Test
    public void contextMenuOutofService()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // put out of service
        MockHelper
            .expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutOutOfService( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.OUT_OF_SERVICE ) );
        // check on UI
        assertThat( repositories.getStatus( hostedRepo.getId() ), equalTo( "Out of Service" ) );

        // back to service
        MockHelper.expect( "/repositories/{repositoryId}/status", new MockResponse( Status.CLIENT_ERROR_BAD_REQUEST,
                                                                                    null ) );
        repositories.contextMenuPutInService( hostedRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuPutInService( hostedRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( hostedRepo.getLocalStatus(), equalTo( LocalStatus.IN_SERVICE ) );
        // check on UI
        assertThat( repositories.getStatus( hostedRepo.getId() ), equalTo( "In Service" ) );
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void contextMenuBlockProxy()
        throws InterruptedException, NoSuchRepositoryException
    {

        RepositoriesTab repositories = startContextMenuTest();

        // block proxy
        MockHelper
            .expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuBlockProxy( proxyRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.BLOCKED_MANUAL ) );
        // check on UI
        assertThat( repositories.getStatus( proxyRepo.getId() ),
                    anyOf( equalTo( "In Service - Remote Manually Blocked and Available" ),
                           equalTo( "In Service - Remote Manually Blocked and Unavailable" ) ) );

        // allow proxy
        MockHelper
            .expect( "/repositories/{repositoryId}/status", new MockResponse( Status.SERVER_ERROR_INTERNAL, null ) );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );
        new MessageBox( selenium ).clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.listen( "/repositories/{repositoryId}/status", new MockListener() );
        repositories.contextMenuAllowProxy( proxyRepo.getId() );
        new Window( selenium ).waitFor();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
        // check on server
        assertThat( proxyRepo.getProxyMode(), equalTo( ProxyMode.ALLOW ) );
        // check on UI
        assertThat( repositories.getStatus( proxyRepo.getId() ),
                    anyOf( equalTo( "In Service" ), equalTo( "In Service - <I>checking remote...</I>" ) ) );
    }
}

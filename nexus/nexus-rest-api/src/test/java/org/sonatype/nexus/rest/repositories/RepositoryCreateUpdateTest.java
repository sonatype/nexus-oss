package org.sonatype.nexus.rest.repositories;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;

public class RepositoryCreateUpdateTest
    extends AbstractNexusTestCase
{
    protected Nexus nexus;

    @Override
    protected boolean loadConfigurationAtSetUp()
    {
        return false;
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexus = lookup( Nexus.class );
        
        nexus.getNexusConfiguration().setSecurityEnabled( false );
    }

    public void testCreate()
        throws Exception
    {

        RepositoryProxyResource result = this.sendAndGetResponse();
        //
        // CHECK RESULTS
        // 

        Assert.assertEquals( "test-id", result.getId() );
        // Assert.assertEquals( true, result.isAllowWrite() );
        Assert.assertEquals( 2, result.getArtifactMaxAge() );
        Assert.assertEquals( true, result.isBrowseable() );
        Assert.assertEquals( ChecksumPolicy.STRICT.name(), result.getChecksumPolicy() );
        Assert.assertEquals( true, result.isDownloadRemoteIndexes() );

        Assert.assertEquals( true, result.isExposed() );
        Assert.assertEquals( "maven2", result.getFormat() );
        Assert.assertEquals( false, result.isIndexable() );
        Assert.assertEquals( 23, result.getMetadataMaxAge() );
        Assert.assertEquals( "test-name", result.getName() );
        Assert.assertEquals( 11, result.getNotFoundCacheTTL() );
        Assert.assertEquals( "maven2", result.getProvider() );
        Assert.assertEquals( RepositoryPolicy.RELEASE.name(), result.getRepoPolicy() );
        Assert.assertEquals( "proxy", result.getRepoType() );

        Assert.assertEquals( "http://foo.com/", result.getRemoteStorage().getRemoteStorageUrl() );

        AuthenticationSettings resultAuth = result.getRemoteStorage().getAuthentication();

        Assert.assertEquals( "ntlmDomain", resultAuth.getNtlmDomain() );
        Assert.assertEquals( "ntlmHost", resultAuth.getNtlmHost() );
        // Assert.assertEquals( "passphrase", resultAuth.getPassphrase() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER, resultAuth.getPassword() );
        // Assert.assertEquals( "privateKey", resultAuth.getPrivateKey() );
        Assert.assertEquals( "username", resultAuth.getUsername() );

        RemoteConnectionSettings resultCon = result.getRemoteStorage().getConnectionSettings();

        Assert.assertEquals( 123, resultCon.getConnectionTimeout() );
        Assert.assertEquals( "queryString", resultCon.getQueryString() );
        Assert.assertEquals( 321, resultCon.getRetrievalRetryCount() );
        Assert.assertEquals( "userAgentString", resultCon.getUserAgentString() );

        Assert.assertEquals( "proxyHostname", result.getRemoteStorage().getHttpProxySettings().getProxyHostname() );
        Assert.assertEquals( 999, result.getRemoteStorage().getHttpProxySettings().getProxyPort() );

        AuthenticationSettings resultAuth2 = result.getRemoteStorage().getHttpProxySettings().getAuthentication();

        Assert.assertEquals( "ntlmDomain2", resultAuth2.getNtlmDomain() );
        Assert.assertEquals( "ntlmHost2", resultAuth2.getNtlmHost() );
        // Assert.assertEquals( "passphrase2", resultAuth2.getPassphrase() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER, resultAuth2.getPassword() );
        // Assert.assertEquals( "privateKey2", resultAuth2.getPrivateKey() );
        Assert.assertEquals( "username2", resultAuth2.getUsername() );
        
        // NEXUS-1994 override local storage should be null
        Assert.assertNull( result.getOverrideLocalStorageUrl() );
        Assert.assertTrue( StringUtils.isNotEmpty( result.getDefaultLocalStorageUrl() ) );
    }

    public void testUpdate()
        throws Exception
    {

        RepositoryProxyResource originalResource = this.sendAndGetResponse();

        originalResource.getRemoteStorage().setRemoteStorageUrl( "http://foo-new.com" );

        AuthenticationSettings authSettings = originalResource.getRemoteStorage().getAuthentication();
        authSettings.setNtlmDomain( "ntlmDomain-new" );
        authSettings.setNtlmHost( "ntlmHost-new" );
        authSettings.setPassword( "password-new" );
        authSettings.setUsername( "username-new" );

        RemoteConnectionSettings connectionSettings = originalResource.getRemoteStorage().getConnectionSettings();
        connectionSettings.setConnectionTimeout( 1232 );
        connectionSettings.setQueryString( "queryString-new" );
        connectionSettings.setRetrievalRetryCount( 3212 );
        connectionSettings.setUserAgentString( "userAgentString-new" );

        RemoteHttpProxySettings httpProxySettings = originalResource.getRemoteStorage().getHttpProxySettings();
        httpProxySettings.setProxyHostname( "proxyHostname-new" );
        httpProxySettings.setProxyPort( 9991 );

        AuthenticationSettings proxyAuthSettings = new AuthenticationSettings();
        httpProxySettings.setAuthentication( proxyAuthSettings );
        proxyAuthSettings.setNtlmDomain( "ntlmDomain2-new" );
        proxyAuthSettings.setNtlmHost( "ntlmHost2-new" );
        proxyAuthSettings.setPassword( "password2-new" );
        proxyAuthSettings.setUsername( "username2-new" );

        RepositoryPlexusResource plexusResource =
            (RepositoryPlexusResource) this.lookup( PlexusResource.class, "RepositoryPlexusResource" );

        Request request = buildRequest();
        Response response = new Response( request );

        request.getAttributes().put( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY, originalResource.getId() );

        RepositoryResourceResponse repoRequest = new RepositoryResourceResponse();
        repoRequest.setData( originalResource );

        RepositoryResourceResponse repoResponse =
            (RepositoryResourceResponse) plexusResource.put( null, request, response, repoRequest );
        RepositoryProxyResource result = (RepositoryProxyResource) repoResponse.getData();

        //
        // now check
        //

        Assert.assertEquals( "test-id", result.getId() );
        // Assert.assertEquals( true, result.isAllowWrite() );
        Assert.assertEquals( 2, result.getArtifactMaxAge() );
        Assert.assertEquals( true, result.isBrowseable() );
        Assert.assertEquals( ChecksumPolicy.STRICT.name(), result.getChecksumPolicy() );
        Assert.assertEquals( true, result.isDownloadRemoteIndexes() );

        Assert.assertEquals( true, result.isExposed() );
        Assert.assertEquals( "maven2", result.getFormat() );
        Assert.assertEquals( false, result.isIndexable() );
        Assert.assertEquals( 23, result.getMetadataMaxAge() );
        Assert.assertEquals( "test-name", result.getName() );
        Assert.assertEquals( 11, result.getNotFoundCacheTTL() );
        Assert.assertEquals( "maven2", result.getProvider() );
        Assert.assertEquals( RepositoryPolicy.RELEASE.name(), result.getRepoPolicy() );
        Assert.assertEquals( "proxy", result.getRepoType() );

        Assert.assertEquals( "http://foo-new.com/", result.getRemoteStorage().getRemoteStorageUrl() );

        AuthenticationSettings resultAuth = result.getRemoteStorage().getAuthentication();

        Assert.assertEquals( "ntlmDomain-new", resultAuth.getNtlmDomain() );
        Assert.assertEquals( "ntlmHost-new", resultAuth.getNtlmHost() );
        // Assert.assertEquals( "passphrase-new", resultAuth.getPassphrase() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER, resultAuth.getPassword() );
        // Assert.assertEquals( "privateKey-new", resultAuth.getPrivateKey() );
        Assert.assertEquals( "username-new", resultAuth.getUsername() );

        RemoteConnectionSettings resultCon = result.getRemoteStorage().getConnectionSettings();

        Assert.assertEquals( 1232, resultCon.getConnectionTimeout() );
        Assert.assertEquals( "queryString-new", resultCon.getQueryString() );
        Assert.assertEquals( 3212, resultCon.getRetrievalRetryCount() );
        Assert.assertEquals( "userAgentString-new", resultCon.getUserAgentString() );

        Assert.assertEquals( "proxyHostname-new", result.getRemoteStorage().getHttpProxySettings().getProxyHostname() );
        Assert.assertEquals( 9991, result.getRemoteStorage().getHttpProxySettings().getProxyPort() );

        AuthenticationSettings resultAuth2 = result.getRemoteStorage().getHttpProxySettings().getAuthentication();

        Assert.assertEquals( "ntlmDomain2-new", resultAuth2.getNtlmDomain() );
        Assert.assertEquals( "ntlmHost2-new", resultAuth2.getNtlmHost() );
        // Assert.assertEquals( "passphrase2-new", resultAuth2.getPassphrase() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER, resultAuth2.getPassword() );
        // Assert.assertEquals( "privateKey2-new", resultAuth2.getPrivateKey() );
        Assert.assertEquals( "username2-new", resultAuth2.getUsername() );
        // NEXUS-1994 override local storage should be null
        Assert.assertNull( result.getOverrideLocalStorageUrl() );
        Assert.assertTrue( StringUtils.isNotEmpty( result.getDefaultLocalStorageUrl() ) );

    }

    private RepositoryProxyResource sendAndGetResponse()
        throws Exception
    {
        RepositoryResourceResponse repoRequest = new RepositoryResourceResponse();

        RepositoryProxyResource repositoryResource = new RepositoryProxyResource();
        repoRequest.setData( repositoryResource );

        repositoryResource.setId( "test-id" );
        repositoryResource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repositoryResource.setArtifactMaxAge( 2 );
        repositoryResource.setBrowseable( true );
        repositoryResource.setChecksumPolicy( ChecksumPolicy.STRICT.name() );
        repositoryResource.setDownloadRemoteIndexes( true );
        repositoryResource.setExposed( true );
        repositoryResource.setFormat( "maven2" );
        repositoryResource.setIndexable( false );
        repositoryResource.setMetadataMaxAge( 23 );
        repositoryResource.setName( "test-name" );
        repositoryResource.setNotFoundCacheTTL( 11 );
        repositoryResource.setProvider( "maven2" );
        repositoryResource.setProviderRole( Repository.class.getName() );
        repositoryResource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repositoryResource.setRepoType( "proxy" );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        repositoryResource.setRemoteStorage( remoteStorage );

        remoteStorage.setRemoteStorageUrl( "http://foo.com" );

        AuthenticationSettings authSettings = new AuthenticationSettings();
        remoteStorage.setAuthentication( authSettings );
        authSettings.setNtlmDomain( "ntlmDomain" );
        authSettings.setNtlmHost( "ntlmHost" );
        authSettings.setPassword( "password" );
        authSettings.setUsername( "username" );

        RemoteConnectionSettings connectionSettings = new RemoteConnectionSettings();
        remoteStorage.setConnectionSettings( connectionSettings );
        connectionSettings.setConnectionTimeout( 123 );
        connectionSettings.setQueryString( "queryString" );
        connectionSettings.setRetrievalRetryCount( 321 );
        connectionSettings.setUserAgentString( "userAgentString" );

        RemoteHttpProxySettings httpProxySettings = new RemoteHttpProxySettings();
        remoteStorage.setHttpProxySettings( httpProxySettings );
        httpProxySettings.setProxyHostname( "proxyHostname" );
        httpProxySettings.setProxyPort( 999 );

        AuthenticationSettings proxyAuthSettings = new AuthenticationSettings();
        httpProxySettings.setAuthentication( proxyAuthSettings );
        proxyAuthSettings.setNtlmDomain( "ntlmDomain2" );
        proxyAuthSettings.setNtlmHost( "ntlmHost2" );
        proxyAuthSettings.setPassword( "password2" );
        proxyAuthSettings.setUsername( "username2" );

        RepositoryListPlexusResource plexusResource =
            (RepositoryListPlexusResource) this.lookup( PlexusResource.class, "RepositoryListPlexusResource" );

        Request request = buildRequest();
        Response response = new Response( request );

        RepositoryResourceResponse repoResponse =
            (RepositoryResourceResponse) plexusResource.post( null, request, response, repoRequest );
        RepositoryProxyResource result = (RepositoryProxyResource) repoResponse.getData();

        return result;
    }

    public void testCreateAuthNoProxy()
        throws Exception
    {

        RepositoryResourceResponse repoRequest = new RepositoryResourceResponse();

        RepositoryProxyResource repositoryResource = new RepositoryProxyResource();
        repoRequest.setData( repositoryResource );

        repositoryResource.setId( "test-id" );
        repositoryResource.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repositoryResource.setArtifactMaxAge( 2 );
        repositoryResource.setBrowseable( true );
        repositoryResource.setChecksumPolicy( ChecksumPolicy.STRICT.name() );
        repositoryResource.setDownloadRemoteIndexes( true );
        repositoryResource.setExposed( true );
        repositoryResource.setFormat( "maven2" );
        repositoryResource.setIndexable( false );
        repositoryResource.setMetadataMaxAge( 23 );
        repositoryResource.setName( "test-name" );
        repositoryResource.setNotFoundCacheTTL( 11 );
        repositoryResource.setProvider( "maven2" );
        repositoryResource.setProviderRole( Repository.class.getName() );
        repositoryResource.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        repositoryResource.setRepoType( "proxy" );

        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        repositoryResource.setRemoteStorage( remoteStorage );

        remoteStorage.setRemoteStorageUrl( "http://foo.com" );

        AuthenticationSettings authSettings = new AuthenticationSettings();
        remoteStorage.setAuthentication( authSettings );
        authSettings.setNtlmDomain( "ntlmDomain" );
        authSettings.setNtlmHost( "ntlmHost" );
        authSettings.setPassword( "password" );
        authSettings.setUsername( "username" );

        RepositoryListPlexusResource plexusResource =
            (RepositoryListPlexusResource) this.lookup( PlexusResource.class, "RepositoryListPlexusResource" );

        Request request = buildRequest();
        Response response = new Response( request );

        RepositoryResourceResponse repoResponse =
            (RepositoryResourceResponse) plexusResource.post( null, request, response, repoRequest );
        RepositoryProxyResource result = (RepositoryProxyResource) repoResponse.getData();

        // 
        // make sure proxy is null
        //

        Assert.assertNull( result.getRemoteStorage().getHttpProxySettings() );

        // now do an update and test again
        RepositoryPlexusResource updateResource =
            (RepositoryPlexusResource) this.lookup( PlexusResource.class, "RepositoryPlexusResource" );

        request.getAttributes().put( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY, result.getId() );

        repoResponse = (RepositoryResourceResponse) updateResource.put( null, request, response, repoResponse );
        result = (RepositoryProxyResource) repoResponse.getData();

        // 
        // make sure proxy is null
        //

        Assert.assertNull( result.getRemoteStorage().getHttpProxySettings() );
        
        // NEXUS-1994 override local storage should be null
        Assert.assertNull( result.getOverrideLocalStorageUrl() );
        Assert.assertTrue( StringUtils.isNotEmpty( result.getDefaultLocalStorageUrl() ) );
    }
    
    public void testUpdateLocalStorage()
        throws Exception
    {

        RepositoryProxyResource originalResource = this.sendAndGetResponse();
        String newlocalStorage =  originalResource.getDefaultLocalStorageUrl().replaceAll( originalResource.getId(), "foo/bar" );
        originalResource.setOverrideLocalStorageUrl(newlocalStorage );
        
        RepositoryPlexusResource plexusResource =
            (RepositoryPlexusResource) this.lookup( PlexusResource.class, "RepositoryPlexusResource" );

        Request request = buildRequest();
        Response response = new Response( request );

        request.getAttributes().put( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY, originalResource.getId() );

        RepositoryResourceResponse repoRequest = new RepositoryResourceResponse();
        repoRequest.setData( originalResource );

        RepositoryResourceResponse repoResponse =
            (RepositoryResourceResponse) plexusResource.put( null, request, response, repoRequest );
        RepositoryProxyResource result = (RepositoryProxyResource) repoResponse.getData();
        
        Assert.assertEquals( newlocalStorage, result.getOverrideLocalStorageUrl() );
        
    }
    
    private Request buildRequest()
    {
        Request request = new Request();
        Reference ref = new Reference( "http://localhost:12345/" );

        request.setRootRef( ref );
        request.setResourceRef( new Reference( ref, "repositories" ) );
        
        return request;
    }

}

package org.sonatype.nexus.template;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

@Component( role = RepositoryTemplateProvider.class )
public class DefaultRepositoryTemplateProvider
    extends AbstractLogEnabled
    implements RepositoryTemplateProvider, Initializable
{

    public static final String HOSTED_ID = "default_hosted_release";

    public static final String PROXY_ID = "default_proxy_release";

    public static final String VIRTUAL_ID = "default_virtual";

    private final Map<String, RepositoryBaseResource> templates = new LinkedHashMap<String, RepositoryBaseResource>();

    public void addTempate( String id, RepositoryBaseResource template )
    {
        templates.put( id, template );
    }

    public void initialize()
        throws InitializationException
    {
        RepositoryShadowResource shadow = new RepositoryShadowResource();
        shadow.setId( VIRTUAL_ID );
        shadow.setName( "Default Virtual Repository Template" );
        shadow.setRepoType( "virtual" );
        shadow.setProvider( "m2-m1-shadow" );
        shadow.setFormat( "maven1" );
        shadow.setSyncAtStartup( false );

        templates.put( VIRTUAL_ID, shadow );

        RepositoryResource hosted = new RepositoryResource();
        hosted.setProvider( "maven2" );
        hosted.setRepoType( "hosted" );
        hosted.setFormat( "maven2" );
        hosted.setId( HOSTED_ID );
        hosted.setName( "Default Release Shadow Repository Template" );
        hosted.setAllowWrite( true );
        hosted.setBrowseable( true );
        hosted.setIndexable( true );
        hosted.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        hosted.setNotFoundCacheTTL( 1440 );

        templates.put( HOSTED_ID, hosted );

        RepositoryProxyResource proxy = new RepositoryProxyResource();
        proxy.setProvider( "maven2" );
        proxy.setRepoType( "proxy" );
        proxy.setFormat( "maven2" );
        proxy.setId( PROXY_ID );
        proxy.setName( "Default Release Proxy Repository Template" );
        proxy.setAllowWrite( true );
        proxy.setBrowseable( true );
        proxy.setIndexable( true );
        proxy.setRepoPolicy( RepositoryPolicy.RELEASE.name() );
        proxy.setNotFoundCacheTTL( 1440 );
        proxy.setArtifactMaxAge( -1 );
        proxy.setMetadataMaxAge( 1440 );
        RepositoryResourceRemoteStorage remoteStorage = new RepositoryResourceRemoteStorage();
        remoteStorage.setRemoteStorageUrl( "http://some-remote-repository/repo-root" );
        proxy.setRemoteStorage( remoteStorage );

        templates.put( PROXY_ID, proxy );

    }

    public RepositoryBaseResource retrieveTemplate( String id )
    {
        if ( templates.containsKey( id ) )
        {
            return templates.get( id );
        }

        return null;
    }

}

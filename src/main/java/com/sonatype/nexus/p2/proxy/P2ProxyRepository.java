/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.mirror.DownloadMirrorSelector;
import org.sonatype.nexus.proxy.mirror.DownloadMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Mirror;
import org.sonatype.nexus.proxy.repository.MutableProxyRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.util.StringDigester;

import com.sonatype.nexus.p2.P2Constants;
import com.sonatype.nexus.p2.P2ContentClass;
import com.sonatype.nexus.p2.P2Repository;
import com.sonatype.nexus.p2.metadata.P2MetadataSource;
import com.sonatype.nexus.p2.proxy.mappings.ArtifactMapping;
import com.sonatype.nexus.p2.proxy.mappings.ArtifactPath;

@Component( role = Repository.class, hint = P2ProxyRepository.ROLE_HINT, instantiationStrategy = "per-lookup", description = "Eclipse P2 Proxy Repository" )
public class P2ProxyRepository
    extends AbstractProxyRepository
    implements P2Repository, Repository
{
    public static final String ROLE_HINT = "p2";

    private static final String PRIVATE_MIRRORS_PATH = P2Constants.PRIVATE_ROOT + "/mirrors.xml";

    @Requirement( hint = P2ContentClass.ID )
    private ContentClass contentClass;

    @Requirement( role = P2MetadataSource.class, hint = "proxy" )
    private P2MetadataSource<P2ProxyRepository> metadataSource;

    @Requirement( role = P2ProxyRepositoryConfigurator.class )
    private P2ProxyRepositoryConfigurator p2ProxyRepositoryConfigurator;

    private volatile boolean mirrorsConfigured;

    private MutableProxyRepositoryKind repositoryKind;

    private P2ProxyDownloadMirrors downloadMirrors;

    public P2ProxyRepository()
    {
        initArtifactMappingsAndMirrors();
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    /**
     * Override the "default" kind with Maven specifics.
     */
    public RepositoryKind getRepositoryKind()
    {
        if ( repositoryKind == null )
        {
            repositoryKind =
                new MutableProxyRepositoryKind( this, null, new DefaultRepositoryKind( HostedRepository.class, null ),
                    new DefaultRepositoryKind( P2ProxyRepository.class, null ) );
        }

        return repositoryKind;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<P2ProxyRepositoryConfiguration>()
        {
            public P2ProxyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new P2ProxyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    @Override
    protected Configurator getConfigurator()
    {
        return p2ProxyRepositoryConfigurator;
    }

    @Override
    protected P2ProxyRepositoryConfiguration getExternalConfiguration( boolean forModification )
    {
        return (P2ProxyRepositoryConfiguration) super.getExternalConfiguration( forModification );
    }

    protected void configureMirrors( final ResourceStoreRequest incomingRequest, final RepositoryItemUid uid )
    {
        getLogger().debug( "Repository " + getId() + ": configureMirrors: mirrorsConfigured=" + mirrorsConfigured );
        AbstractStorageItem mirrorsItem = null;

        // Try to get the mirrors from local storage
        try
        {
            ResourceStoreRequest request = new ResourceStoreRequest( PRIVATE_MIRRORS_PATH );
            mirrorsItem = getLocalStorage().retrieveItem( this, request );
        }
        catch ( StorageException e )
        {
            // fall through
        }
        catch ( ItemNotFoundException e )
        {
            // fall through
        }

        if ( mirrorsConfigured && ( mirrorsItem == null || !isOld( mirrorsItem ) ) )
        {
            return;
        }

        // exclusive locking begins here, since actual work needs to be done
        final RepositoryItemUidLock lock = uid.getLock();
        
        lock.lock( Action.create );

        try
        {
            // Try to get the mirrors from remote
            if ( mirrorsItem == null || isOld( mirrorsItem ) )
            {
                this.mirrorsURLsByRepositoryURL = null;

                getLogger().debug( "Repository " + getId() + ": configureMirrors: getting mirrors from remote" );
                ResourceStoreRequest request = new ResourceStoreRequest( P2Constants.ARTIFACTS_XML );
                request.getRequestContext().setParentContext( incomingRequest.getRequestContext() );

                StorageItem artifacts = retrieveItem( request );

                // The P2ProxyMetadataSource.ATTR_MIRRORS_URL attribute of the artifacts StorageItem
                // is set in the P2ProxyMetadataSource.doRetrieveArtifactsDom().
                // The attribute is set only if the remote repository is a SimpleArtifactRepository (i.e. it is not set
                // for CompositeArtifactRepositories)
                String mirrorsURL = artifacts.getAttributes().get( P2ProxyMetadataSource.ATTR_MIRRORS_URL );
                if ( mirrorsURL != null )
                {
                    // The remote repository is a SimpleArtifactRepository with mirrors configured
                    getLogger().debug(
                        "Repository " + getId() + ": configureMirrors: found single mirrors URL=" + mirrorsURL );
                    mirrorsItem = getMirrorsItemRemote( mirrorsURL );
                    mirrorsItem.setRepositoryItemUid( createUid( PRIVATE_MIRRORS_PATH ) );
                    mirrorsItem = doCacheItem( mirrorsItem );
                }
                else
                {
                    mirrorsItem = getMirrorsItemRemote();
                    if ( mirrorsItem == null )
                    {
                        mirrorsConfigured = true;
                        return;
                    }
                }
            }

            Xpp3Dom mirrorsDom = getMirrorsDom( (StorageFileItem) mirrorsItem );
            Xpp3Dom[] repositoryDoms = mirrorsDom.getChildren( "repository" );
            if ( repositoryDoms != null && repositoryDoms.length > 0 )
            {
                for ( Xpp3Dom repositoryDom : repositoryDoms )
                {
                    String repositoryUrl = repositoryDom.getAttribute( "uri" );
                    Xpp3Dom[] mirrorsDoms = repositoryDom.getChildren( "mirror" );
                    addMirrors( repositoryUrl, mirrorsDoms );
                }
            }
            else
            {
                getLogger().debug( "Repository " + getId() + ": configureMirrors: found flat list of mirrors" );
                // There are no "repository" elements, so we only have a flat list of mirrors
                List<Mirror> mirrors = new ArrayList<Mirror>();

                for ( Xpp3Dom mirrorDOM : mirrorsDom.getChildren( "mirror" ) )
                {
                    String mirrorUrl = mirrorDOM.getAttribute( "url" );
                    getLogger().debug( "Repository " + getId() + ": configureMirrors: found mirror URL=" + mirrorUrl );
                    if ( mirrorUrl != null )
                    {
                        // TODO: validate that this is valid way to generate id
                        // or if should be pulled from xml
                        mirrors.add( new Mirror( StringDigester.getSha1Digest( mirrorUrl ), mirrorUrl,
                            this.getRemoteUrl() ) );
                    }
                }

                getDownloadMirrors().setMirrors( mirrors );
                getApplicationConfiguration().saveConfiguration();
            }

            mirrorsConfigured = true;
        }
        catch ( Exception e )
        {
            getLogger().warn(
                "Could not retrieve list of repository mirrors. All downloads will come from repository canonical URL",
                e );
        }
        finally
        {
            lock.unlock();
        }
    }

    private void addMirrors( String remoteRepositoryUrl, Xpp3Dom[] mirrorsDoms )
    {
        if ( mirrorsDoms != null )
        {
            for ( Xpp3Dom mirrorDOM : mirrorsDoms )
            {
                String mirrorUrl = mirrorDOM.getAttribute( "url" );
                if ( mirrorUrl != null )
                {
                    // TODO: validate that this is valid way to generate id
                    // or if should be pulled from xml
                    this.getP2DownloadMirrors().addMirror(
                        new Mirror( StringDigester.getSha1Digest( mirrorUrl ), mirrorUrl, remoteRepositoryUrl ) );
                }
            }
        }
        this.getP2DownloadMirrors().addMirror(
            new Mirror( StringDigester.getSha1Digest( remoteRepositoryUrl ), remoteRepositoryUrl, remoteRepositoryUrl ) );
    }

    private Xpp3Dom getMirrorsDom( StorageFileItem mirrorsItem )
        throws IOException, XmlPullParserException
    {
        InputStream is = mirrorsItem.getInputStream();

        try
        {
            return Xpp3DomBuilder.build( new XmlStreamReader( is ) );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    private AbstractStorageItem getMirrorsItemRemote()
        throws IllegalOperationException, ItemNotFoundException, IOException, XmlPullParserException
    {
        Map<String, String> mirrorsURLsMap = getMirrorsURLsByRepositoryURL();
        if ( mirrorsURLsMap == null )
        {
            getLogger().debug( "getMirrorsItemRemote: mirrorsURLsMap is null" );
            return null;
        }

        Xpp3Dom mirrorsByRepositoryDom = new Xpp3Dom( "mirrors" );
        for ( String repositoryURL : mirrorsURLsMap.keySet() )
        {
            getLogger().debug( "getMirrorsItemRemote: repositoryURL=" + repositoryURL );
            Xpp3Dom repositoryDom = new Xpp3Dom( "repository" );
            repositoryDom.setAttribute( "uri", repositoryURL );
            mirrorsByRepositoryDom.addChild( repositoryDom );

            String mirrorsURL = mirrorsURLsMap.get( repositoryURL );
            if ( mirrorsURL == null )
            {
                continue;
            }

            AbstractStorageItem mirrorsItem = getMirrorsItemRemote( mirrorsURL );
            Xpp3Dom mirrorsDom = getMirrorsDom( (StorageFileItem) mirrorsItem );
            for ( Xpp3Dom mirrorDOM : mirrorsDom.getChildren( "mirror" ) )
            {
                getLogger().debug( "getMirrorsItemRemote: mirrorURL=" + mirrorDOM.getAttribute( "url" ) );
                repositoryDom.addChild( mirrorDOM );
            }
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        MXSerializer mx = new MXSerializer();
        mx.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  " );
        mx.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
        String encoding = "UTF-8";
        mx.setOutput( buffer, encoding );
        mx.startDocument( encoding, null );
        mirrorsByRepositoryDom.writeToSerializer( null, mx );
        mx.flush();

        byte[] bytes = buffer.toByteArray();

        ContentLocator content = new ByteArrayContentLocator( bytes, "text/xml" );
        DefaultStorageFileItem result =
            new DefaultStorageFileItem( this, new ResourceStoreRequest( PRIVATE_MIRRORS_PATH ), true /* isReadable */,
                false /* isWritable */, content );
        result.setLength( bytes.length );
        doCacheItem( result );
        return result;
    }

    private AbstractStorageItem getMirrorsItemRemote( String mirrorsURL )
        throws MalformedURLException, RemoteAccessException, StorageException, ItemNotFoundException
    {
        URL url = new URL( mirrorsURL );

        ResourceStoreRequest request = new ResourceStoreRequest( url.getFile() );

        String baseUrl = getBaseMirrorsURL( url );
        AbstractStorageItem mirrorsItem = getRemoteStorage().retrieveItem( this, request, baseUrl );

        return mirrorsItem;
    }

    private String getBaseMirrorsURL( URL mirrorsURL )
    {
        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append( mirrorsURL.getProtocol() ).append( "://" );
        if ( mirrorsURL.getUserInfo() != null )
        {
            baseUrl.append( mirrorsURL.getUserInfo() ).append( "@" );
        }
        baseUrl.append( mirrorsURL.getHost() );
        if ( mirrorsURL.getPort() != -1 )
        {
            baseUrl.append( ":" ).append( mirrorsURL.getPort() );
        }

        return baseUrl.toString();
    }

    @Override
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        final RepositoryItemUid uid = createUid( P2Constants.METADATA_LOCK_PATH );
        final RepositoryItemUidLock lock = uid.getLock();

        // NOTE: THIS IS A DIRTY HACK
        // We are doing this, to serialize the access to P2Proxy repository,
        // and just exclude any possibility of deadlocks for now.
        // Naturally, we need to come up with proper fix for P2Proxy repository implementation.
        lock.lock( Action.create );

        try
        {
            return super.retrieveItem( fromTask, request );
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        String requestPath = request.getRequestPath();
        getLogger().debug( "Repository " + getId() + ": doRetrieveItem:" + requestPath );

        if ( P2Constants.ARTIFACT_MAPPINGS_XML.equals( requestPath ) )
        {
            if ( getLocalStorage() == null )
            {
                throw new ItemNotFoundException( request );
            }

            StorageItem item = getLocalStorage().retrieveItem( this, request );
            item.getItemContext().putAll( request.getRequestContext() );
            return item;
        }

        StorageItem item = metadataSource.doRetrieveItem( request, this );
        if ( item != null )
        {
            return item;
        }

        // The request is not for a metadata file if we are here.
        // Lock the metadata do be sure that the artifacts are retrieved from consistent paths.
        final RepositoryItemUid uid = createUid( P2Constants.METADATA_LOCK_PATH );
        final RepositoryItemUidLock lock = uid.getLock();

        // NOTE - THIS CANNOT be a write action (create/delete/update) as that will force a write lock
        // thus serializing access to this p2 repo. Using a read action here, will block the file from
        // being deleted/updated while retrieving the remote item, and that is all we need.

        // NXCM-2499 temporarily we do put access serialization back here, to avoid all the deadlocks.
        lock.lock( Action.create );
        try
        {
            // note this method can potentially go retrieve new mirrors, but it is using locking, so no
            // need to worry about multiples getting in
            configureMirrors( request, uid );
            return super.doRetrieveItem( request );
        }
        finally
        {
            lock.unlock();
        }
    }

    private volatile Map<String, String> mirrorsURLsByRepositoryURL;

    private Map<String, String> getMirrorsURLsByRepositoryURL()
        throws IllegalOperationException, StorageException
    {
        if ( !hasArtifactMappings )
        {
            return null;
        }

        if ( mirrorsURLsByRepositoryURL == null )
        {
            loadArtifactMappings();
        }
        return mirrorsURLsByRepositoryURL;
    }

    private volatile Map<String, ArtifactMapping> remoteArtifactMappings;

    private volatile boolean hasArtifactMappings;

    /* package */void initArtifactMappingsAndMirrors()
    {
        hasArtifactMappings = true;
        remoteArtifactMappings = null;
        mirrorsURLsByRepositoryURL = null;
        mirrorsConfigured = false;
    }

    public Map<String, ArtifactMapping> getArtifactMappings()
        throws IllegalOperationException, StorageException
    {
        // cstamas: this method is called from other paths, not like getMirrorsURLsByRepositoryURL() above (called from
        // configureMirrors()), so the safest is to protect it with similar locking stuff even if we do suffer in
        // performance, but we avoiding potential deadlocks (this method was synchronized).
        final RepositoryItemUid uid = createUid( P2Constants.METADATA_LOCK_PATH );
        final RepositoryItemUidLock lock = uid.getLock();

        lock.lock( Action.create );

        try
        {
            if ( !hasArtifactMappings )
            {
                return null;
            }

            if ( remoteArtifactMappings == null )
            {
                loadArtifactMappings();
            }
            return remoteArtifactMappings;
        }
        finally
        {
            lock.unlock();
        }
    }

    private void loadArtifactMappings()
        throws StorageException, IllegalOperationException
    {
        StorageFileItem artifactMappingsItem;
        ResourceStoreRequest req = new ResourceStoreRequest( P2Constants.ARTIFACT_MAPPINGS_XML );
        req.setRequestLocalOnly( true );
        try
        {
            artifactMappingsItem = (StorageFileItem) retrieveItem( true, req );
        }
        catch ( ItemNotFoundException e )
        {
            hasArtifactMappings = false;
            return;
        }

        Map<String, ArtifactMapping> tempRemoteArtifactMappings = new LinkedHashMap<String, ArtifactMapping>();
        Map<String, String> tempMirrorsURLsByRepositoryURL = new LinkedHashMap<String, String>();
        Xpp3Dom dom;
        try
        {
            dom = Xpp3DomBuilder.build( new XmlStreamReader( artifactMappingsItem.getInputStream() ) );
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not load artifact mappings", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new StorageException( "Could not load artifact mappings", e );
        }
        Xpp3Dom[] artifactRepositories = dom.getChildren( "repository" );
        for ( Xpp3Dom artifactRepositoryDom : artifactRepositories )
        {
            String repositoryUri = artifactRepositoryDom.getAttribute( "uri" );

            Map<String, ArtifactPath> artifactPaths = new LinkedHashMap<String, ArtifactPath>();
            ArtifactMapping artifactMapping = new ArtifactMapping( repositoryUri, artifactPaths );
            for ( Xpp3Dom artifactDom : artifactRepositoryDom.getChildren( "artifact" ) )
            {
                artifactPaths.put( artifactDom.getAttribute( "remotePath" ),
                    new ArtifactPath( artifactDom.getAttribute( "remotePath" ), artifactDom.getAttribute( "md5" ) ) );
            }
            tempRemoteArtifactMappings.put( repositoryUri, artifactMapping );

            String mirrorsURL = artifactRepositoryDom.getAttribute( P2Constants.PROP_MIRRORS_URL );
            tempMirrorsURLsByRepositoryURL.put( repositoryUri, mirrorsURL );
        }

        remoteArtifactMappings = tempRemoteArtifactMappings;
        mirrorsURLsByRepositoryURL = tempMirrorsURLsByRepositoryURL;
    }

    @Override
    public DownloadMirrors getDownloadMirrors()
    {
        return this.getP2DownloadMirrors();
    }

    private P2ProxyDownloadMirrors getP2DownloadMirrors()
    {
        if ( downloadMirrors == null )
        {
            downloadMirrors = new P2ProxyDownloadMirrors();
        }

        return downloadMirrors;
    }

    @Override
    protected DownloadMirrorSelector openDownloadMirrorSelector( ResourceStoreRequest request )
    {
        String remoteUrl = this.getRemoteUrl();

        // lookup child from the map here then udpate the remote URL
        try
        {
            Map<String, ArtifactMapping> artifactMappings = getArtifactMappings();
            if ( artifactMappings != null )
            {
                for ( String remoteRepositoryURI : artifactMappings.keySet() )
                {
                    if ( artifactMappings.get( remoteRepositoryURI ).getArtifactsPath().containsKey(
                        request.getRequestPath() ) )
                    {
                        remoteUrl = remoteRepositoryURI;
                        break;
                    }
                }
            }
        }
        catch ( StorageException e )
        {
            this.getLogger().warn( "Could not find artifact-mapping.", e );
        }
        catch ( IllegalOperationException e )
        {
            this.getLogger().warn( "Could not find artifact-mapping.", e );
        }

        // now open the selector
        return this.getDownloadMirrors().openSelector( remoteUrl );
    }

    @Override
    protected boolean isRemoteStorageReachable( ResourceStoreRequest request )
        throws StorageException, RemoteAuthenticationNeededException, RemoteAccessDeniedException
    {
        // For p2 repositories, the root URL may not be reachable,
        // so we test if we can reach one of the "standard" p2 repository metadata files.
        for ( String metadataFilePath : P2Constants.METADATA_FILE_PATHS )
        {
            getLogger().debug(
                "isRemoteStorageReachable: RepositoryId=" + getId() + ": Trying to access " + metadataFilePath );
            request.setRequestPath( metadataFilePath );
            try
            {
                // We cannot use getRemoteStorage().isReachable() here because that forces the request path to be "/"
                if ( getRemoteStorage().containsItem( this, request ) )
                {
                    getLogger().debug(
                        "isRemoteStorageReachable: RepositoryId=" + getId() + ": Successfully accessed "
                            + metadataFilePath );
                    return true;
                }
            }
            catch ( Exception e )
            {
                getLogger().debug(
                    "isRemoteStorageReachable: RepositoryId=" + getId() + ": Caught exception while trying to access "
                        + metadataFilePath, e );
            }
        }

        return false;
    }

    public int getArtifactMaxAge()
    {
        return getExternalConfiguration( false ).getArtifactMaxAge();
    }

    public void setArtifactMaxAge( int maxAge )
    {
        getExternalConfiguration( true ).setArtifactMaxAge( maxAge );
    }

    public int getMetadataMaxAge()
    {
        return getExternalConfiguration( false ).getMetadataMaxAge();
    }

    public void setMetadataMaxAge( int metadataMaxAge )
    {
        getExternalConfiguration( true ).setMetadataMaxAge( metadataMaxAge );
    }

    @Override
    public boolean isOld( StorageItem item )
    {
        if ( P2ProxyMetadataSource.isP2MetadataItem( item.getPath() ) )
        {
            return super.isOld( getMetadataMaxAge(), item );
        }
        else
        {
            return super.isOld( getArtifactMaxAge(), item );
        }
    }

    public ChecksumPolicy getChecksumPolicy()
    {
        return getExternalConfiguration( false ).getChecksumPolicy();
    }

    public void setChecksumPolicy( ChecksumPolicy checksumPolicy )
    {
        getExternalConfiguration( true ).setChecksumPolicy( checksumPolicy );
    }

}

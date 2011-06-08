/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.proxy;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.metadata.AbstractP2MetadataSource;
import org.sonatype.nexus.plugins.p2.repository.metadata.Artifacts;
import org.sonatype.nexus.plugins.p2.repository.metadata.Content;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataSource;
import org.sonatype.nexus.plugins.p2.repository.util.P2Util;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;

import com.sonatype.nexus.p2.facade.P2Facade;

@Component( role = P2MetadataSource.class, hint = "proxy" )
public class P2ProxyMetadataSource
    extends AbstractP2MetadataSource<P2ProxyRepository>
    implements Initializable
{

    @Requirement
    private P2Facade p2;

    public static final String ATTR_MIRRORS_URL = P2Constants.PROP_MIRRORS_URL;

    public static final String CTX_MIRRORS_URL = P2Constants.PROP_MIRRORS_URL;

    public void initialize()
        throws InitializationException
    {
        p2.initializeP2( P2Util.getPluginCoordinates() );
    }

    @Override
    protected Xpp3Dom doRetrieveArtifactsDom( Map<String, Object> context, P2ProxyRepository repository )
        throws StorageException, ItemNotFoundException
    {
        Xpp3Dom dom;

        try
        {
            File file = File.createTempFile( "artifacts", ".xml" );
            File artifactMappingsXmlFile = File.createTempFile( "p2proxy.artifact-mappings", ".xml" );
            try
            {
                String username = null;
                String password = null;
                RemoteAuthenticationSettings remoteAuthenticationSettings =
                    repository.getRemoteAuthenticationSettings();
                if ( remoteAuthenticationSettings instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    UsernamePasswordRemoteAuthenticationSettings upras =
                        (UsernamePasswordRemoteAuthenticationSettings) remoteAuthenticationSettings;
                    username = upras.getUsername();
                    password = upras.getPassword();
                }

                p2.getRepositoryArtifacts( getRemoteUrl( repository ), username, password, file,
                    artifactMappingsXmlFile );

                dom = Xpp3DomBuilder.build( new XmlStreamReader( file ) );
                storeItemFromFile( P2Constants.ARTIFACT_MAPPINGS_XML, artifactMappingsXmlFile, repository );
                repository.initArtifactMappingsAndMirrors();
            }
            finally
            {
                file.delete();
                artifactMappingsXmlFile.delete();
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new StorageException( e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            throw new StorageException( e );
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }

        Artifacts artifacts = new Artifacts( dom );

        artifacts.setRepositoryAttributes( getName( repository ) );

        LinkedHashMap<String, String> properties = artifacts.getProperties();

        String mirrorsURL = properties.get( P2Constants.PROP_MIRRORS_URL );
        if ( mirrorsURL != null )
        {
            context.put( CTX_MIRRORS_URL, mirrorsURL );
        }

        properties.remove( P2Constants.PROP_MIRRORS_URL );
        boolean compressed = P2Constants.ARTIFACTS_PATH.equals( P2Constants.ARTIFACTS_JAR );
        properties.put( P2Constants.PROP_COMPRESSED, Boolean.toString( compressed ) );
        // properties.put( P2Facade.PROP_REPOSITORY_ID, getId( repository ) );
        artifacts.setProperties( properties );

        return artifacts.getDom();
    }

    private String getRemoteUrl( P2ProxyRepository repository )
    {
        return ( repository ).getRemoteUrl();
    }

    @Override
    protected Xpp3Dom doRetrieveContentDom( Map<String, Object> context, P2ProxyRepository repository )
        throws StorageException, ItemNotFoundException
    {
        Xpp3Dom dom;

        try
        {
            File file = File.createTempFile( "content", ".xml" );
            try
            {
                String username = null;
                String password = null;
                RemoteAuthenticationSettings remoteAuthenticationSettings =
                    repository.getRemoteAuthenticationSettings();
                if ( remoteAuthenticationSettings instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    UsernamePasswordRemoteAuthenticationSettings upras =
                        (UsernamePasswordRemoteAuthenticationSettings) remoteAuthenticationSettings;
                    username = upras.getUsername();
                    password = upras.getPassword();
                }

                p2.getRepositoryContent( getRemoteUrl( repository ), username, password, file );

                dom = Xpp3DomBuilder.build( new XmlStreamReader( file ) );
            }
            finally
            {
                file.delete();
            }
        }
        catch ( XmlPullParserException e )
        {
            throw new StorageException( e );
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }

        Content content = new Content( dom );

        content.setRepositoryAttributes( getName( repository ) );

        LinkedHashMap<String, String> properties = content.getProperties();
        properties.remove( P2Constants.PROP_MIRRORS_URL );
        boolean compressed = P2Constants.CONTENT_PATH.equals( P2Constants.CONTENT_JAR );
        properties.put( P2Constants.PROP_COMPRESSED, Boolean.toString( compressed ) );
        // properties.put( P2Facade.PROP_REPOSITORY_ID, getId( repository ) );
        content.setProperties( properties );

        return content.getDom();
    }

    @Override
    protected StorageItem doRetrieveRemoteItem( Repository repository, String path, Map<String, Object> context )
        throws ItemNotFoundException, RemoteAccessException, StorageException
    {
        P2ProxyRepository repo = (P2ProxyRepository) repository;
        // always return metadata from canonical url
        return getRemoteStorage( repo ).retrieveItem( repo, new ResourceStoreRequest( path ), getRemoteUrl( repo ) );
    }

    public RemoteRepositoryStorage getRemoteStorage( P2ProxyRepository repo )
    {
        return ( repo ).getRemoteStorage();
    }

    @Override
    protected void setItemAttributes( StorageFileItem item, Map<String, Object> context, P2ProxyRepository repository )
    {
        String mirrorsURL = (String) context.get( CTX_MIRRORS_URL );
        if ( mirrorsURL != null )
        {
            item.getAttributes().put( ATTR_MIRRORS_URL, mirrorsURL );
        }
    }

    @Override
    protected boolean isArtifactsOld( AbstractStorageItem artifactsItem, P2ProxyRepository repository )
        throws StorageException
    {
        return repository.isOld( artifactsItem );
    }

    @Override
    protected boolean isContentOld( AbstractStorageItem contentItem, P2ProxyRepository repository )
        throws StorageException
    {
        return repository.isOld( contentItem );
    }

    private void storeItemFromFile( String path, File file, P2ProxyRepository repository )
        throws StorageException, UnsupportedStorageOperationException
    {
        ContentLocator content = new FileContentLocator( file, "text/xml" );
        DefaultStorageFileItem storageItem =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( path ), true /* isReadable */,
                false /* isWritable */, content );
        getLocalStorage( repository ).storeItem( repository, storageItem );
    }
}

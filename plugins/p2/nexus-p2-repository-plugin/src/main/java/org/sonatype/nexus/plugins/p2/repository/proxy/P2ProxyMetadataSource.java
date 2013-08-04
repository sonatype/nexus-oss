/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.p2.repository.proxy;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.P2ProxyRepository;
import org.sonatype.nexus.plugins.p2.repository.metadata.AbstractP2MetadataSource;
import org.sonatype.nexus.plugins.p2.repository.metadata.Artifacts;
import org.sonatype.nexus.plugins.p2.repository.metadata.Content;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataSource;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.MetadataRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role = P2MetadataSource.class, hint = "proxy")
public class P2ProxyMetadataSource
    extends AbstractP2MetadataSource<P2ProxyRepository>
{

  @Requirement
  private ArtifactRepository artifactRepository;

  @Requirement
  private MetadataRepository metadataRepository;

  public static final String ATTR_MIRRORS_URL = P2Constants.PROP_MIRRORS_URL;

  public static final String CTX_MIRRORS_URL = P2Constants.PROP_MIRRORS_URL;

  @Override
  protected StorageFileItem doRetrieveArtifactsFileItem(final Map<String, Object> context,
                                                        final P2ProxyRepository repository)
      throws RemoteStorageException, ItemNotFoundException
  {
    Xpp3Dom dom;

    try {
      final File artifactRepositoryDir = File.createTempFile("artifacts", "");
      artifactRepositoryDir.delete();
      artifactRepositoryDir.mkdirs();

      final File artifactMappingsXmlFile = File.createTempFile("p2proxy.artifact-mappings", ".xml");
      try {
        String username = null;
        String password = null;
        final RemoteAuthenticationSettings remoteAuthenticationSettings =
            repository.getRemoteAuthenticationSettings();
        if (remoteAuthenticationSettings instanceof UsernamePasswordRemoteAuthenticationSettings) {
          final UsernamePasswordRemoteAuthenticationSettings upras =
              (UsernamePasswordRemoteAuthenticationSettings) remoteAuthenticationSettings;
          username = upras.getUsername();
          password = upras.getPassword();
        }

        try {
          artifactRepository.createProxyRepository(new URI(repository.getRemoteUrl()), username, password,
              artifactRepositoryDir.toURI(), artifactMappingsXmlFile);
        }
        catch (RuntimeException e) {
          // another implementation detail: M2 proxy with bad URL would just bore you with 404s
          // while P2 would return HTTP 500 Internal Error
          // while the path will not be quite exact (we are NOT asking for XML but P2 engine does multiple
          // queries, but the meaning is fine for now
          throw new P2RuntimeExceptionMaskedAsINFException(new ResourceStoreRequest(repository.getRemoteUrl()
              + "artifacts.xml"), repository, e);
        }

        dom = Xpp3DomBuilder.build(new XmlStreamReader(new File(artifactRepositoryDir, "artifacts.xml")));
        storeItemFromFile(P2Constants.ARTIFACT_MAPPINGS_XML, artifactMappingsXmlFile, repository);
        repository.initArtifactMappingsAndMirrors();
      }
      finally {
        FileUtils.deleteDirectory(artifactRepositoryDir);
        artifactMappingsXmlFile.delete();
      }
    }
    catch (final XmlPullParserException e) {
      throw new RemoteStorageException(e);
    }
    catch (final UnsupportedStorageOperationException e) {
      throw new RemoteStorageException(e);
    }
    catch (final URISyntaxException e) {
      throw new RemoteStorageException(e);
    }
    catch (final IOException e) {
      throw new RemoteStorageException(e);
    }

    final Artifacts artifacts = new Artifacts(dom);
    artifacts.setRepositoryAttributes(repository.getName());
    final LinkedHashMap<String, String> properties = artifacts.getProperties();
    final String mirrorsURL = properties.get(P2Constants.PROP_MIRRORS_URL);
    if (mirrorsURL != null) {
      context.put(CTX_MIRRORS_URL, mirrorsURL);
    }

    properties.remove(P2Constants.PROP_MIRRORS_URL);
    final boolean compressed = P2Constants.ARTIFACTS_PATH.equals(P2Constants.ARTIFACTS_JAR);
    properties.put(P2Constants.PROP_COMPRESSED, Boolean.toString(compressed));
    // properties.put( P2Facade.PROP_REPOSITORY_ID, getId( repository ) );
    artifacts.setProperties(properties);

    try {
      final StorageFileItem artifactsItem =
          createMetadataItem(repository, P2Constants.ARTIFACTS_XML, artifacts.getDom(),
              P2Constants.XMLPI_ARTIFACTS, context);
      return artifactsItem;
    }
    catch (IOException e) {
      throw new RemoteStorageException(e);
    }
  }

  @Override
  protected StorageFileItem doRetrieveContentFileItem(final Map<String, Object> context,
                                                      final P2ProxyRepository repository)
      throws RemoteStorageException, ItemNotFoundException
  {
    Xpp3Dom dom;

    try {
      final File metadataRepositoryDir = File.createTempFile("content", "");
      metadataRepositoryDir.delete();
      metadataRepositoryDir.mkdirs();

      try {
        String username = null;
        String password = null;
        final RemoteAuthenticationSettings remoteAuthenticationSettings =
            repository.getRemoteAuthenticationSettings();
        if (remoteAuthenticationSettings instanceof UsernamePasswordRemoteAuthenticationSettings) {
          final UsernamePasswordRemoteAuthenticationSettings upras =
              (UsernamePasswordRemoteAuthenticationSettings) remoteAuthenticationSettings;
          username = upras.getUsername();
          password = upras.getPassword();
        }

        try {
          metadataRepository.createProxyRepository(new URI(repository.getRemoteUrl()), username, password,
              metadataRepositoryDir.toURI());
        }
        catch (RuntimeException e) {
          // another implementation detail: M2 proxy with bad URL would just bore you with 404s
          // while P2 would return HTTP 500 Internal Error
          // while the path will not be quite exact (we are NOT asking for XML but P2 engine does multiple
          // queries, but the meaning is fine for now
          throw new P2RuntimeExceptionMaskedAsINFException(new ResourceStoreRequest(repository.getRemoteUrl()
              + "content.xml"), repository, e);
        }

        dom = Xpp3DomBuilder.build(new XmlStreamReader(new File(metadataRepositoryDir, "content.xml")));
      }
      finally {
        FileUtils.deleteDirectory(metadataRepositoryDir);
      }
    }
    catch (final XmlPullParserException e) {
      throw new RemoteStorageException(e);
    }
    catch (final URISyntaxException e) {
      throw new RemoteStorageException(e);
    }
    catch (final IOException e) {
      throw new RemoteStorageException(e);
    }

    final Content content = new Content(dom);
    content.setRepositoryAttributes(repository.getName());
    final LinkedHashMap<String, String> properties = content.getProperties();
    properties.remove(P2Constants.PROP_MIRRORS_URL);
    final boolean compressed = P2Constants.CONTENT_PATH.equals(P2Constants.CONTENT_JAR);
    properties.put(P2Constants.PROP_COMPRESSED, Boolean.toString(compressed));
    // properties.put( P2Facade.PROP_REPOSITORY_ID, getId( repository ) );
    content.setProperties(properties);

    try {
      final StorageFileItem contentItem =
          createMetadataItem(repository, P2Constants.CONTENT_XML, content.getDom(), P2Constants.XMLPI_CONTENT,
              context);
      return contentItem;
    }
    catch (IOException e) {
      throw new RemoteStorageException(e);
    }
  }

  protected StorageItem doRetrieveRemoteItem(final P2ProxyRepository repository, final String path,
                                             final Map<String, Object> context)
      throws ItemNotFoundException, RemoteAccessException, RemoteStorageException
  {
    return repository.getRemoteStorage().retrieveItem(repository, new ResourceStoreRequest(path),
        repository.getRemoteUrl());
  }

  @Override
  protected void setItemAttributes(final StorageFileItem item, final Map<String, Object> context,
                                   final P2ProxyRepository repository)
  {
    final String mirrorsURL = (String) context.get(CTX_MIRRORS_URL);
    if (mirrorsURL != null) {
      item.getRepositoryItemAttributes().put(ATTR_MIRRORS_URL, mirrorsURL);
    }
  }

  @Override
  protected boolean isArtifactsOld(final AbstractStorageItem artifactsItem, final P2ProxyRepository repository) {
    return repository.isMetadataOld(artifactsItem);
  }

  @Override
  protected boolean isContentOld(final AbstractStorageItem contentItem, final P2ProxyRepository repository) {
    return repository.isMetadataOld(contentItem);
  }

  private void storeItemFromFile(final String path, final File file, final P2ProxyRepository repository)
      throws LocalStorageException, UnsupportedStorageOperationException
  {
    final ContentLocator content = new FileContentLocator(file, "text/xml");
    final DefaultStorageFileItem storageItem =
        new DefaultStorageFileItem(repository, new ResourceStoreRequest(path), true /* isReadable */,
            false /* isWritable */, content);
    repository.getLocalStorage().storeItem(repository, storageItem);
  }
}

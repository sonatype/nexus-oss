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

package org.sonatype.nexus.plugins.p2.repository.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.P2GroupRepository;
import org.sonatype.nexus.plugins.p2.repository.metadata.AbstractP2MetadataSource;
import org.sonatype.nexus.plugins.p2.repository.metadata.Artifacts;
import org.sonatype.nexus.plugins.p2.repository.metadata.ArtifactsMerge;
import org.sonatype.nexus.plugins.p2.repository.metadata.Content;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataMergeException;
import org.sonatype.nexus.plugins.p2.repository.metadata.P2MetadataSource;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupItemNotFoundException;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = P2MetadataSource.class, hint = "group")
public class P2GroupMetadataSource
    extends AbstractP2MetadataSource<P2GroupRepository>
{
  private static final String ATTR_HASH_PREFIX = "original";

  @Override
  protected StorageFileItem doRetrieveArtifactsFileItem(final Map<String, Object> context,
                                                        final P2GroupRepository repository)
      throws RemoteStorageException, ItemNotFoundException
  {
    try {
      final List<StorageFileItem> fileItems = doRetrieveItems(P2Constants.ARTIFACTS_XML, context, repository);
      final ArtifactsMerge m = new ArtifactsMerge();
      final Artifacts metadata = m.mergeArtifactsMetadata(repository.getName(), fileItems);

      final LinkedHashMap<String, String> properties = metadata.getProperties();
      // properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
      metadata.setProperties(properties);

      final StorageFileItem mergedItem =
          createMetadataItem(repository, P2Constants.ARTIFACTS_XML, metadata.getDom(),
              P2Constants.XMLPI_ARTIFACTS, context);

      return mergedItem;
    }
    catch (final P2MetadataMergeException e) {
      throw new RemoteStorageException(e);
    }
    catch (IOException e) {
      throw new RemoteStorageException(e);
    }
  }

  @Override
  protected StorageFileItem doRetrieveContentFileItem(final Map<String, Object> context,
                                                      final P2GroupRepository repository)
      throws RemoteStorageException, ItemNotFoundException
  {
    try {
      final List<StorageFileItem> fileItems = doRetrieveItems(P2Constants.CONTENT_XML, context, repository);
      final ArtifactsMerge m = new ArtifactsMerge();
      final Content metadata = m.mergeContentMetadata(repository.getName(), fileItems);

      final LinkedHashMap<String, String> properties = metadata.getProperties();
      // properties.put( P2Facade.PROP_REPOSITORY_ID, repository.getId() );
      metadata.setProperties(properties);

      final StorageFileItem mergedItem =
          createMetadataItem(repository, P2Constants.CONTENT_XML, metadata.getDom(), P2Constants.XMLPI_CONTENT,
              context);

      return mergedItem;
    }
    catch (final P2MetadataMergeException e) {
      throw new RemoteStorageException(e);
    }
    catch (final IOException e) {
      throw new RemoteStorageException(e);
    }
  }

  private List<StorageFileItem> doRetrieveItems(final String xmlName, final Map<String, Object> context,
                                                final P2GroupRepository repository)
      throws IOException, GroupItemNotFoundException
  {
    final ResourceStoreRequest request = new ResourceStoreRequest(xmlName);
    request.getRequestContext().putAll(context);
    final List<StorageItem> items = repository.doRetrieveItems(request);

    final ArrayList<StorageFileItem> fileItems = new ArrayList<StorageFileItem>(items.size());
    for (StorageItem item : items) {
      if (item instanceof StorageFileItem) {
        fileItems.add((StorageFileItem) item);
      }
    }
    return fileItems;
  }

  @Override
  protected boolean isArtifactsOld(final AbstractStorageItem artifactsItem, final P2GroupRepository repository) {
    final Map<String, Object> context = new HashMap<String, Object>();
    return isOld(artifactsItem, P2Constants.ARTIFACTS_XML, context, repository);
  }

  @Override
  protected boolean isContentOld(final AbstractStorageItem contentItem, final P2GroupRepository repository) {
    final Map<String, Object> context = new HashMap<String, Object>();
    return isOld(contentItem, P2Constants.CONTENT_XML, context, repository);
  }

  @Override
  protected void setItemAttributes(final StorageFileItem item, final Map<String, Object> context,
                                   final P2GroupRepository repository)
  {
    if (P2Constants.ARTIFACTS_JAR.equals(item.getPath()) || P2Constants.ARTIFACTS_XML.equals(item.getPath())) {
      item.getRepositoryItemAttributes().putAll(getMemberHash(P2Constants.ARTIFACTS_XML, context, repository));
    }
    else if (P2Constants.CONTENT_JAR.equals(item.getPath()) || P2Constants.CONTENT_XML.equals(item.getPath())) {
      item.getRepositoryItemAttributes().putAll(getMemberHash(P2Constants.CONTENT_XML, context, repository));
    }
  }

  private boolean isOld(final AbstractStorageItem artifactsItem, final String xml,
                        final Map<String, Object> context, final P2GroupRepository repository)
  {
    final TreeMap<String, String> memberHash = getMemberHash(xml, context, repository);
    final LinkedHashMap<String, String> hash = new LinkedHashMap<String, String>();
    final Map<String, String> attributes = artifactsItem.getRepositoryItemAttributes().asMap();
    for (final Map.Entry<String, String> entry : attributes.entrySet()) {
      if (entry.getKey().startsWith(ATTR_HASH_PREFIX)) {
        hash.put(entry.getKey(), entry.getValue());
      }
    }
    return !hash.equals(memberHash);
  }

  private TreeMap<String, String> getMemberHash(final String xml, final Map<String, Object> context,
                                                final P2GroupRepository repository)
  {
    final TreeMap<String, String> memberHash = new TreeMap<String, String>();
    int count = 0;
    List<StorageFileItem> storageItems;
    try {
      storageItems = doRetrieveItems(xml, context, repository);
    }
    catch (final Exception e) {
      // assume it has changed, so return an empty map
      return memberHash;
    }

    for (final StorageFileItem storageItem : storageItems) {
      final String hash =
          storageItem.getRepositoryItemAttributes().get(DigestCalculatingInspector.DIGEST_SHA1_KEY);
      if (hash != null) {
        memberHash.put(ATTR_HASH_PREFIX + count + "." + storageItem.getRepositoryItemUid().toString(), hash);
        count++;
      }
    }

    return memberHash;
  }
}

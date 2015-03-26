/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.hash.Hashes;
import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupFacetImpl;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.internal.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MetadataMerger;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MetadataMerger.MetadataEnvelope;
import org.sonatype.nexus.repository.storage.AssetEvent;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.hash.HashCode;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Maven specific implementation of {@link GroupFacetImpl}.
 *
 * @since 3.0
 */
@Named
@Facet.Exposed
public class MavenGroupFacet
    extends GroupFacetImpl
{
  private final Maven2MetadataMerger metadataMerger = new Maven2MetadataMerger();

  private MavenFacet mavenFacet;

  @Inject
  public MavenGroupFacet(final RepositoryManager repositoryManager) {
    super(repositoryManager);
  }

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    this.mavenFacet = getRepository().facet(MavenFacet.class);
  }

  /**
   * Fetches cached metadata if exists, or {@code null}.
   */
  @Nullable
  public MavenContent getCachedMergedMetadata(final MavenPath mavenPath) throws IOException {
    return mavenFacet.get(mavenPath);
  }

  /**
   * Merges and caches and returns the merged metadata. Returns {@code null} if no usable response was in passed in
   * map.
   */
  @Nullable
  public MavenContent mergeAndCacheMetadata(final MavenPath mavenPath,
                                            final LinkedHashMap<Repository, Response> responses) throws IOException
  {
    checkArgument(!mavenPath.isHash(), "Only metadata can be merged and cached!");
    final LinkedHashMap<Repository, MavenContent> metadataPayloads = Maps.newLinkedHashMap();
    for (Map.Entry<Repository, Response> entry : responses.entrySet()) {
      if (entry.getValue().getStatus().getCode() == HttpStatus.OK) {
        final Response response = entry.getValue();
        if (response instanceof PayloadResponse && ((PayloadResponse) response).getPayload() instanceof MavenContent) {
          metadataPayloads.put(entry.getKey(), (MavenContent) ((PayloadResponse) response).getPayload());
        }
      }
    }

    if (metadataPayloads.isEmpty()) {
      return null;
    }
    final MavenContent mergedMetadata = mergeMetadata(mavenPath, metadataPayloads);
    if (mergedMetadata == null) {
      return null;
    }
    return cacheMergedMetadata(mavenPath, mergedMetadata);
  }

  private MavenContent mergeMetadata(final MavenPath mavenPath,
                                     final LinkedHashMap<Repository, MavenContent> metadataPayloads) throws IOException
  {
    if (metadataPayloads.size() == 1) {
      return metadataPayloads.get(0);
    }
    final MetadataXpp3Reader reader = new MetadataXpp3Reader();
    List<MetadataEnvelope> metadatas = Lists.newArrayList();
    for (Map.Entry<Repository, MavenContent> entry : metadataPayloads.entrySet()) {
      final String origin = entry.getKey().getName() + " @ " + mavenPath.getPath();
      try (InputStream inputStream = entry.getValue().openInputStream()) {
        final Metadata metadata = reader.read(inputStream);
        metadatas.add(new MetadataEnvelope(origin, metadata));
      }
      catch (XmlPullParserException e) {
        // skip it, log it
        log.info("Unparseable repository metadata: {}", origin, e);
      }
    }

    final Metadata mergedMetadata = metadataMerger.merge(metadatas.toArray(new MetadataEnvelope[metadatas.size()]));
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    new MetadataXpp3Writer().write(byteArrayOutputStream, mergedMetadata);
    // Metadata is rather small, so let's do it like this
    final Map<HashAlgorithm, HashCode> hashes = Hashes.hash(mavenFacet.getHashAlgorithms(),
        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
    return new MavenContent(
        new BytesPayload(
            byteArrayOutputStream.toByteArray(),
            "text/xml"
        ),
        DateTime.now(),
        hashes.get(HashAlgorithm.SHA1).toString(),
        hashes
    );
  }

  private MavenContent cacheMergedMetadata(final MavenPath mavenPath, final MavenContent payload) throws IOException {
    // put the metadata and then put hashes
    mavenFacet.put(mavenPath, payload);
    // get it and store corresponding hashes
    return mavenFacet.get(mavenPath);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onAssetEvent(final AssetEvent event) {
    if (member(event.getRepository())) {
      final String path = event.getAsset().require(StorageFacet.P_PATH);
      final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
      if (mavenFacet.getMavenPathParser().isRepositoryMetadata(mavenPath) && !mavenPath.isSubordinate()) {
        try {
          mavenFacet.delete(mavenPath, mavenPath.hash(HashType.SHA1), mavenPath.hash(HashType.MD5));
        }
        catch (IOException e) {
          log.warn("Could not evict merged metadata from {} cache at {}", getRepository().getName(),
              mavenPath.getPath(), e);
        }
      }
    }
  }
}

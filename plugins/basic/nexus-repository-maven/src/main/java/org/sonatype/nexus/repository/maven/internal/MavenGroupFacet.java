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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupFacetImpl;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.internal.MavenPath.HashType;
import org.sonatype.nexus.repository.storage.AssetEvent;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.BlobPayload;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import com.google.common.hash.HashCode;

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

  public MavenPayload getCachedMergedMetadata(final MavenPath mavenPath) throws IOException {
    return mavenFacet.get(mavenPath);
  }

  public MavenPayload mergeAndCacheMetadata(final MavenPath mavenPath,
                                            final LinkedHashMap<Repository, Response> responses) throws IOException
  {
    checkArgument(!mavenPath.isHash(), "Only metadata can be merged and cached!");
    Payload mergedMetadata = null;
    for (Map.Entry<Repository, Response> entry : responses.entrySet()) {
      if (entry.getValue().getStatus().getCode() == HttpStatus.OK) {
        final Response response = entry.getValue();
        if (response instanceof PayloadResponse) {
          final PayloadResponse payloadResponse = (PayloadResponse) response;

          // TODO: do the merge, this is just fluke to work in testing
          if (mergedMetadata == null) {
            mergedMetadata = payloadResponse.getPayload();
          }
        }
      }
    }
    if (mergedMetadata == null) {
      return null;
    }

    cacheMergedMetadata(mavenPath, mergedMetadata);

    return getCachedMergedMetadata(mavenPath);
  }

  private void cacheMergedMetadata(final MavenPath mavenPath, final Payload payload) throws IOException {
    // put the metadata and then put hashes
    mavenFacet.put(mavenPath, payload);
    final MavenPayload blobPayload = mavenFacet.get(mavenPath);
    final HashCode sha1HashCode = blobPayload.getHashCodes().get(HashAlgorithm.SHA1);
    mavenFacet.put(mavenPath.hash(HashType.SHA1), new StringPayload(sha1HashCode.toString(), "text/plain"));
    final HashCode md5HashCode = blobPayload.getHashCodes().get(HashAlgorithm.MD5);
    mavenFacet.put(mavenPath.hash(HashType.MD5), new StringPayload(md5HashCode.toString(), "text/plain"));
  }

  // TODO: asset event listening and using MavenFacet.delete maybe to delete invalidated elements
  //@Subscribe
  public void onAssetEvent(final AssetEvent event) {
    // IF event coming from a member
    // AND event is for a metadata MavenPath
    // THEN mavenFacet.delete
  }
}

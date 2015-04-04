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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.maven.internal.MavenPath.HashType;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Headers;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.PayloadResponse;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;

import com.google.common.hash.HashCode;
import org.joda.time.DateTime;

/**
 * Maven specific implementation of {@link ProxyFacetSupport}.
 *
 * @since 3.0
 */
@Named
public class MavenProxyFacet
    extends ProxyFacetSupport
{
  private MavenFacet mavenFacet;

  @Override
  protected void doConfigure() throws Exception {
    super.doConfigure();
    this.mavenFacet = getRepository().facet(MavenFacet.class);
  }

  @Override
  protected Content getCachedPayload(final Context context) throws IOException {
    return mavenFacet.get(mavenPath(context));
  }

  @Override
  protected void store(final Context context, final Content payload) throws IOException, InvalidContentException {
    mavenFacet.put(mavenPath(context), payload);
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context) throws IOException {
    return mavenFacet.getLastVerified(mavenPath(context));
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    mavenFacet.setLastVerified(mavenPath(context), new DateTime());
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return context.getRequest().getPath().substring(1); // omit leading slash
  }

  @Override
  protected Map<HashAlgorithm, HashCode> getExpectedHashes(final Context context) {
    MavenPath mavenPath = mavenPath(context);
    if (mavenPath.isHash()) {
      return null;
    }
    Map<HashAlgorithm, HashCode> hashes = new HashMap<>();
    HashCode hash = getHash(mavenPath, HashType.SHA1);
    if (hash != null) {
      hashes.put(HashAlgorithm.SHA1, hash);
    }
    else {
      hash = getHash(mavenPath, HashType.MD5);
      if (hash != null) {
        hashes.put(HashAlgorithm.MD5, hash);
      }
    }
    return hashes;
  }

  private HashCode getHash(final MavenPath mavenPath, final HashType hashType) {
    MavenPath hashMavenPath = mavenPath.hash(hashType);
    // TODO use a request builder
    Request request = new Request(hashMavenPath.getPath())
    {
      {
        action = HttpMethods.GET;
        parameters = new Parameters();
        headers = new Headers();
      }
    };
    try {
      Response response = getRepository().facet(ViewFacet.class).dispatch(request);
      if (response instanceof PayloadResponse) {
        Payload payload = ((PayloadResponse) response).getPayload();
        String hashCode = DigestExtractor.extract(payload.openInputStream());
        if (hashCode != null) {
          return HashCode.fromString(hashCode);
        }
      }
    }
    catch (Exception e) {
      // ignore
    }
    return null;
  }

  @Nonnull
  private MavenPath mavenPath(final @Nonnull Context context) {
    return context.getAttributes().require(MavenPath.class);
  }
}

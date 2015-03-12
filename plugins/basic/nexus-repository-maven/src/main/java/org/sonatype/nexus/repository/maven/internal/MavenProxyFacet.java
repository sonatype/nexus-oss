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

import javax.annotation.Nonnull;
import javax.inject.Named;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.view.Context;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkState;

/**
 * Maven proxy facet that talks to {@link MavenFacet} and {@link MavenTransportFacet}.
 *
 * @since 3.0
 */
@Named
public class MavenProxyFacet
    extends ProxyFacetSupport<MavenPayload>
{
  @Override
  protected MavenPayload getCachedPayload(final Context context) throws IOException {
    final ArtifactPath artifactPath = context.getAttributes().get(ArtifactPath.class);
    if (artifactPath != null) {
      return getRepository().facet(MavenFacet.class).getArtifact(artifactPath);
    }
    final Path metadataPath = context.getAttributes().get(Path.class);
    if (metadataPath != null) {
      return getRepository().facet(MavenFacet.class).getMetadata(metadataPath);
    }
    return null;
  }

  @Override
  protected MavenPayload fetch(final String url, final Context context, final MavenPayload stalePayload)
      throws IOException
  {
    Path path = context.getAttributes().get(Path.class);
    if (path == null) {
      path = context.getAttributes().get(ArtifactPath.class);
    }
    checkState(path != null);

    final MavenPayload payload = getRepository().facet(MavenTransportFacet.class)
        .fetch(path, getRemoteUrl().resolve(url), stalePayload);
    if (stalePayload != null && payload == stalePayload) {
      indicateUpToDate(context);
    }
    return payload;
  }

  @Override
  protected void store(final Context context, final MavenPayload payload) throws IOException, InvalidContentException {
    final ArtifactPath artifactPath = context.getAttributes().get(ArtifactPath.class);
    if (artifactPath != null) {
      getRepository().facet(MavenFacet.class).putArtifact(artifactPath, payload);
      return;
    }
    final Path metadataPath = context.getAttributes().get(Path.class);
    if (metadataPath != null) {
      getRepository().facet(MavenFacet.class).putMetadata(metadataPath, payload);
      return;
    }
    // TODO: what here?
    throw new IllegalArgumentException("How did we get here? Bad route?");
  }

  @Override
  protected DateTime getCachedPayloadLastUpdatedDate(final Context context, final MavenPayload payload)
      throws IOException
  {
    return payload.getLastUpdated();
  }

  @Override
  protected void indicateUpToDate(final Context context) throws IOException {
    // TODO: update P_LAST_UPDATED
  }

  @Override
  protected String getUrl(final @Nonnull Context context) {
    return context.getRequest().getPath().substring(1);
  }
}

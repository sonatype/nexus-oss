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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.content.InvalidContentException;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.view.Context;

/**
 * Maven hosted artifact handler.
 *
 * @since 3.0
 */
@Singleton
@Named
public class HostedArtifactHandler
    extends HostedHandlerSupport<ArtifactPath>
{
  @Override
  protected ArtifactPath getPath(final Context context) throws IllegalArgumentException {
    final ArtifactPath artifactPath = context.getAttributes().require(ArtifactPath.class);
    final VersionPolicy versionPolicy = context.getRepository().facet(MavenFacet.class).getVersionPolicy();
    if (!allowsArtifactRepositoryPath(versionPolicy, artifactPath)) {
      throw new IllegalArgumentException(
          "Repository version policy: " + versionPolicy + " does not allow version: " + artifactPath.getVersion());
    }
    return artifactPath;
  }

  @Override
  protected MavenPayload get(final Context context, final ArtifactPath path) throws IOException {
    return context.getRepository().facet(MavenFacet.class).getArtifact(path);
  }

  @Override
  protected void put(final Context context, final ArtifactPath path, final MavenPayload payload)
      throws IOException, InvalidContentException
  {
    context.getRepository().facet(MavenFacet.class).putArtifact(path, payload);
  }

  @Override
  protected boolean delete(final Context context, final ArtifactPath path) throws IOException {
    return context.getRepository().facet(MavenFacet.class).deleteArtifact(path);
  }

  // ==

  private boolean allowsArtifactRepositoryPath(final VersionPolicy versionPolicy, final ArtifactPath path) {
    if (versionPolicy == VersionPolicy.SNAPSHOT) {
      return path.isSnapshot();
    }
    if (versionPolicy == VersionPolicy.RELEASE) {
      return !path.isSnapshot();
    }
    return true;
  }
}

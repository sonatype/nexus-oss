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

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.maven.internal.policy.VersionPolicy;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static org.sonatype.nexus.repository.http.HttpMethods.DELETE;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;

/**
 * Maven artifact handler.
 *
 * @since 3.0
 */
@Singleton
@Named
public class MavenArtifactHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(final @Nonnull Context context) throws Exception {
    final String action = context.getRequest().getAction();
    final MavenFacet mavenFacet = context.getRepository().facet(MavenFacet.class);
    final ArtifactPath artifactPath = context.getAttributes().require(ArtifactPath.class);

    final VersionPolicy versionPolicy = context.getRepository().facet(MavenFacet.class).getVersionPolicy();
    if (!allowsArtifactRepositoryPath(versionPolicy, artifactPath)) {
      return HttpResponses.badRequest(
          "Repository version policy: " + versionPolicy + " does not allow version: " + artifactPath.getVersion());
    }
    switch (action) {
      case GET: {
        final Payload content = mavenFacet.getArtifact(artifactPath);
        if (content == null) {
          return HttpResponses.notFound(artifactPath.getPath());
        }
        return HttpResponses.ok(content);
      }

      case PUT: {
        mavenFacet.putArtifact(artifactPath, context.getRequest().getPayload());
        return HttpResponses.created();
      }

      case DELETE: {
        final boolean deleted = mavenFacet.deleteArtifact(artifactPath);
        if (!deleted) {
          return HttpResponses.notFound(artifactPath.getPath());
        }
        return HttpResponses.noContent();
      }

      default:
        return HttpResponses.methodNotAllowed(context.getRequest().getAction(), GET, PUT, DELETE);
    }
  }

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

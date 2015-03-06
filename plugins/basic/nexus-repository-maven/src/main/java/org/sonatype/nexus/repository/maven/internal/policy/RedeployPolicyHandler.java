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
package org.sonatype.nexus.repository.maven.internal.policy;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.maven.internal.ArtifactCoordinates;
import org.sonatype.nexus.repository.maven.internal.MavenFacet;
import org.sonatype.nexus.repository.maven.internal.storage.MavenContentsFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.nexus.repository.maven.internal.ContextHelper.artifactCoordinates;

/**
 * Handler enforcing redeploy policy on Maven repositories.
 *
 * @since 3.0
 */
@Singleton
@Named
public class RedeployPolicyHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    if (PUT.equals(context.getRequest().getAction())) {
      final ArtifactCoordinates artifactCoordinates = artifactCoordinates(context);
      if (artifactCoordinates != null) {
        final boolean contentExists = context.getRepository().facet(MavenContentsFacet.class)
            .exists(artifactCoordinates);
        final RedeployPolicy redeployPolicy = context.getRepository().facet(MavenFacet.class).getRedeployPolicy();
        if (contentExists && RedeployPolicy.DISALLOW.equals(redeployPolicy)) {
          return HttpResponses.badRequest("Artifact redeploy not allowed");
        }
      }
    }
    return context.proceed();
  }
}

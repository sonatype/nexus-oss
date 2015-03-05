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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.VersionPolicy;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static org.sonatype.nexus.repository.maven.internal.ContentHelper.contentName;

/**
 * Handler enforcing repository policy on Maven repositories.
 *
 * @since 3.0
 */
@Singleton
@Named
public class MavenRepositoryPolicyHandler
    extends ComponentSupport
    implements Handler
{
  @Nonnull
  @Override
  public Response handle(@Nonnull final Context context) throws Exception {
    final MavenFacet mavenFacet = context.getRepository().facet(MavenFacet.class);
    final Map<String, String> coordinates = mavenFacet.getPathParser().parsePath(contentName(context));
    if (coordinates != null) {
      final VersionPolicy versionPolicy = mavenFacet.getVersionPolicy();
      final String version = coordinates.get(PathParser.VERSION);
      if (!versionPolicy.allowsVersion(version)) {
        return HttpResponses.badRequest(
            "Repository policy '" + versionPolicy + "' does not allow version '" + version + "'");
      }
    }
    return context.proceed();
  }
}

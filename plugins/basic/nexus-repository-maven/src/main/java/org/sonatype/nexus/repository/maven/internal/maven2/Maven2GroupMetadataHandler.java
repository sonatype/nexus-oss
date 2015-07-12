/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.internal.maven2;

import java.util.LinkedHashMap;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupHandler;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Response;

/**
 * Maven2 specific group metadata handler: metadata merge is specific to Maven2 format.
 *
 * @since 3.0
 */
@Singleton
@Named
public class Maven2GroupMetadataHandler
    extends GroupHandler
{
  @Override
  protected Response doGet(@Nonnull final Context context, @Nonnull final DispatchedRepositories dispatched)
      throws Exception
  {
    final MavenPath mavenPath = context.getAttributes().require(MavenPath.class);
    final Maven2GroupFacet groupFacet = context.getRepository().facet(Maven2GroupFacet.class);
    // get cached one
    Content content = groupFacet.getCachedMergedMetadata(mavenPath);
    if (content != null) {
      return HttpResponses.ok(content);
    }

    // not have it, let's figure out
    if (mavenPath.isHash()) {
      // hash will be available if corresponding metadata fetched. out of bound request?
      return HttpResponses.notFound();
    }
    else {
      // metadata, merge and cache it and get it
      final LinkedHashMap<Repository, Response> responses =
          getAll(context.getRequest(), groupFacet.members(), dispatched);
      content = groupFacet.mergeAndCacheMetadata(mavenPath, responses);
      if (content != null) {
        return HttpResponses.ok(content);
      }
      return HttpResponses.notFound();
    }
  }
}

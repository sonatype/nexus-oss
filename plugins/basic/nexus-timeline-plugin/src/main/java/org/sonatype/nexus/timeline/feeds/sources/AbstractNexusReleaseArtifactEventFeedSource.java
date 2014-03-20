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

package org.sonatype.nexus.timeline.feeds.sources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.timeline.feeds.FeedRecorder;
import org.sonatype.nexus.timeline.feeds.NexusArtifactEvent;

import com.google.common.collect.Sets;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractNexusReleaseArtifactEventFeedSource
    extends AbstractNexusItemEventFeedSource
{
  private final RepositoryRegistry repositoryRegistry;

  protected AbstractNexusReleaseArtifactEventFeedSource(
      final ApplicationStatusSource applicationStatusSource,
      final FeedRecorder feedRecorder,
      final SyndEntryBuilder<NexusArtifactEvent> syndEntryBuilder,
      final String feedKey, final String feedName,
      final String feedDescription,
      final RepositoryRegistry repositoryRegistry)
  {
    super(applicationStatusSource, feedRecorder, syndEntryBuilder, feedKey, feedName, feedDescription);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  @Override
  protected Set<String> getRepoIdsFromParams(final Map<String, String> params) {
    final Set<String> result = Sets.newHashSet();
    final List<Repository> repos = repositoryRegistry.getRepositories();
    for (Repository repo : repos) {
      final MavenRepository mavenRepository = repo.adaptToFacet(MavenRepository.class);
      if (mavenRepository != null) {
        if (RepositoryPolicy.RELEASE == mavenRepository.getRepositoryPolicy()) {
          result.add(repo.getId());
        }
      }
    }
    return result;
  }
}

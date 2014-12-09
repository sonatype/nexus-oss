/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.tasks;

import java.util.List;

import javax.inject.Named;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;

import org.codehaus.plexus.util.StringUtils;

/**
 * Rebuild Maven metadata task.
 */
@Named
public class RebuildMavenMetadataTask
    extends RepositoryTaskSupport<Void>
{
  @Override
  public Void execute()
      throws Exception
  {
    ResourceStoreRequest req = new ResourceStoreRequest(getConfiguration().getPath());

    // no repo id, then do all repos
    if (StringUtils.isEmpty(getConfiguration().getRepositoryId())) {
      List<MavenRepository> reposes = getRepositoryRegistry().getRepositoriesWithFacet(MavenRepository.class);
      for (MavenRepository repo : reposes) {
        log.info("Recreating Maven Metadata on {}", repo);
        repo.recreateMavenMetadata(req);
      }
    }
    else {
      Repository repository = getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId());

      // is this a Maven repository at all?
      if (repository.getRepositoryKind().isFacetAvailable(MavenRepository.class)) {
        MavenRepository repo = repository.adaptToFacet(MavenRepository.class);
        log.info("Recreating Maven Metadata on {}", repo);
        repo.recreateMavenMetadata(req);
      }
      else {
        log.info(
            "Repository {} is not a Maven repository. Will not rebuild maven metadata, but the task seems wrongly configured!",
            repository);
      }
    }
    return null;
  }

  @Override
  public String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Rebuilding maven metadata of repository " + getConfiguration().getRepositoryId() + " from path "
          + getConfiguration().getPath() + " and below.";
    }
    else {
      return "Rebuilding maven metadata of all registered repositories from path " + getConfiguration().getPath() +
          " and below.";
    }
  }
}

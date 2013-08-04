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

package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryDistributedMetadataManager;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.proxy.walker.PredicatePathWalkerFilter;
import org.sonatype.nexus.proxy.walker.WalkerFilter;

import com.google.common.base.Predicate;

/**
 * MavenRepositoryMetadataManager specialization manages Maven2 layouted repositories.
 *
 * @author cstamas
 * @since 2.1
 */
public class MavenRepositoryMetadataManager
    extends AbstractRepositoryDistributedMetadataManager
    implements MetadataManager
{
  public MavenRepositoryMetadataManager(final MavenRepository repository) {
    super(repository);
  }

  @Override
  public boolean recreateMetadata(ResourceStoreRequest request) {
    return getRepository().recreateMavenMetadata(request);
  }

  protected MavenRepository getRepository() {
    return (MavenRepository) super.getRepository();
  }

  @Override
  protected WalkerFilter getMetadataWalkerFilter() {
    return new PredicatePathWalkerFilter(PredicatePathWalkerFilter.ITEM_PATH_EXTRACTOR, new Predicate<String>()
    {
      @Override
      public boolean apply(final String input) {
        return M2ArtifactRecognizer.isMetadata(input);
      }
    });
  }

  @Override
  public void deployArtifact(ArtifactStoreRequest request)
      throws IOException
  {
    if (getRepository() != request.getMavenRepository()) {
      throw new IllegalArgumentException("This is MavenRepositoryMetadataManager of repository "
          + RepositoryStringUtils.getHumanizedNameString(getRepository()) + " while the request refers to "
          + RepositoryStringUtils.getHumanizedNameString(request.getMavenRepository()) + "!");
    }

    getRepository().getMetadataManager().deployArtifact(request);
  }

  @Override
  public void undeployArtifact(ArtifactStoreRequest request)
      throws IOException
  {
    if (getRepository() != request.getMavenRepository()) {
      throw new IllegalArgumentException("This is MavenRepositoryMetadataManager of repository "
          + RepositoryStringUtils.getHumanizedNameString(getRepository()) + " while the request refers to "
          + RepositoryStringUtils.getHumanizedNameString(request.getMavenRepository()) + "!");
    }

    getRepository().getMetadataManager().undeployArtifact(request);
  }

  @Override
  public Gav resolveArtifact(ArtifactStoreRequest request)
      throws IOException
  {
    if (getRepository() != request.getMavenRepository()) {
      throw new IllegalArgumentException("This is MavenRepositoryMetadataManager of repository "
          + RepositoryStringUtils.getHumanizedNameString(getRepository()) + " while the request refers to "
          + RepositoryStringUtils.getHumanizedNameString(request.getMavenRepository()) + "!");
    }

    return getRepository().getMetadataManager().resolveArtifact(request);
  }

  @Override
  public Gav resolveSnapshot(ArtifactStoreRequest request, Gav gav)
      throws IOException
  {
    if (getRepository() != request.getMavenRepository()) {
      throw new IllegalArgumentException("This is MavenRepositoryMetadataManager of repository "
          + RepositoryStringUtils.getHumanizedNameString(getRepository()) + " while the request refers to "
          + RepositoryStringUtils.getHumanizedNameString(request.getMavenRepository()) + "!");
    }

    return getRepository().getMetadataManager().resolveSnapshot(request, gav);
  }
}

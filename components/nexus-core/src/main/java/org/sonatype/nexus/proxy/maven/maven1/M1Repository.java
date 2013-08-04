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

package org.sonatype.nexus.proxy.maven.maven1;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M1ArtifactRecognizer;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * The default M1Repository. This class adds snapshot/release sensing and differentiated expiration handling and repo
 * policies for them.
 *
 * @author cstamas
 */
@Component(role = Repository.class, hint = M1Repository.ID, instantiationStrategy = "per-lookup",
    description = "Maven1 Repository")
public class M1Repository
    extends AbstractMavenRepository
{
  /**
   * This "mimics" the @Named("maven1")
   */
  public static final String ID = Maven1ContentClass.ID;

  /**
   * The GAV Calculator.
   */
  @Requirement(hint = "maven1")
  private GavCalculator gavCalculator;

  @Requirement(hint = Maven1ContentClass.ID)
  private ContentClass contentClass;

  @Requirement
  private M1RepositoryConfigurator m1RepositoryConfigurator;

  @Override
  public M1RepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (M1RepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<M1RepositoryConfiguration> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<M1RepositoryConfiguration>()
    {
      public M1RepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new M1RepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

  public GavCalculator getGavCalculator() {
    return gavCalculator;
  }

  @Override
  protected Configurator getConfigurator() {
    return m1RepositoryConfigurator;
  }

  @Override
  public boolean isMavenMetadataPath(String path) {
    return M1ArtifactRecognizer.isMetadata(path);
  }

  @Override
  public boolean isMavenArtifactChecksumPath(String path) {
    return M1ArtifactRecognizer.isChecksum(path);
  }

  /**
   * Should serve by policies.
   *
   * @param request the request
   * @return true, if successful
   */
  @Override
  public boolean shouldServeByPolicies(ResourceStoreRequest request) {
    if (M1ArtifactRecognizer.isMetadata(request.getRequestPath())) {
      if (M1ArtifactRecognizer.isSnapshot(request.getRequestPath())) {
        return RepositoryPolicy.SNAPSHOT.equals(getRepositoryPolicy());
      }
      else {
        // metadatas goes always
        return true;
      }
    }

    // we are using Gav to test the path
    final Gav gav = getGavCalculator().pathToGav(request.getRequestPath());

    if (gav == null) {
      return true;
    }
    else {
      if (gav.isSnapshot()) {
        // snapshots goes if enabled
        return RepositoryPolicy.SNAPSHOT.equals(getRepositoryPolicy());
      }
      else {
        return RepositoryPolicy.RELEASE.equals(getRepositoryPolicy());
      }
    }
  }

  @Override
  protected boolean isOld(StorageItem item) {
    if (M1ArtifactRecognizer.isMetadata(item.getPath())) {
      return isOld(getMetadataMaxAge(), item);
    }
    if (M1ArtifactRecognizer.isSnapshot(item.getPath())) {
      return isOld(getArtifactMaxAge(), item);
    }

    // we are using Gav to test the path
    final Gav gav = gavCalculator.pathToGav(item.getPath());

    if (gav == null) {
      // this is not an artifact, it is just any "file"
      return super.isOld(item);
    }

    // it is a release
    return isOld(getArtifactMaxAge(), item);
  }

  // not available on maven1 repo
  public boolean recreateMavenMetadata(String path) {
    return false;
  }

  @Override
  protected void enforceWritePolicy(ResourceStoreRequest request, Action action)
      throws IllegalRequestException
  {
    // allow updating of metadata
    // we also need to allow updating snapshots
    if (!M1ArtifactRecognizer.isMetadata(request.getRequestPath())
        && !M1ArtifactRecognizer.isSnapshot(request.getRequestPath())) {
      super.enforceWritePolicy(request, action);
    }
  }

}

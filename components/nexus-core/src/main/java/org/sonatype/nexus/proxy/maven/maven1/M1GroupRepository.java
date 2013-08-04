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
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M1ArtifactRecognizer;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@Component(role = GroupRepository.class, hint = M1GroupRepository.ID, instantiationStrategy = "per-lookup",
    description = "Maven1 Repository Group")
public class M1GroupRepository
    extends AbstractMavenGroupRepository
{
  /**
   * This "mimics" the @Named("maven1")
   */
  public static final String ID = Maven1ContentClass.ID;

  @Requirement(hint = Maven1ContentClass.ID)
  private ContentClass contentClass;

  @Requirement(hint = "maven1")
  private GavCalculator gavCalculator;

  @Requirement
  private M1GroupRepositoryConfigurator m1GroupRepositoryConfigurator;

  @Override
  protected M1GroupRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (M1GroupRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<M1GroupRepositoryConfiguration> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<M1GroupRepositoryConfiguration>()
    {
      public M1GroupRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new M1GroupRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
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
    return m1GroupRepositoryConfigurator;
  }

  public boolean isMavenMetadataPath(String path) {
    return M1ArtifactRecognizer.isMetadata(path);
  }
}

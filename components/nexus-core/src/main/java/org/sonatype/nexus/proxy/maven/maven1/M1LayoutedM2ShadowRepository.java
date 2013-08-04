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

import java.util.List;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M1ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * A shadow repository that transforms M2 layout of master to M1 layouted shadow.
 *
 * @author cstamas
 */
@Component(role = ShadowRepository.class, hint = M1LayoutedM2ShadowRepository.ID, instantiationStrategy = "per-lookup",
    description = "Maven2 to Maven1")
public class M1LayoutedM2ShadowRepository
    extends LayoutConverterShadowRepository
{
  /**
   * This "mimics" the @Named("m2-m1-shadow")
   */
  public static final String ID = "m2-m1-shadow";

  @Requirement(hint = Maven1ContentClass.ID)
  private ContentClass contentClass;

  @Requirement(hint = Maven2ContentClass.ID)
  private ContentClass masterContentClass;

  @Requirement
  private M1LayoutedM2ShadowRepositoryConfigurator m1LayoutedM2ShadowRepositoryConfigurator;

  @Override
  public M1LayoutedM2ShadowRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (M1LayoutedM2ShadowRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<M1LayoutedM2ShadowRepositoryConfiguration> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<M1LayoutedM2ShadowRepositoryConfiguration>()
    {
      public M1LayoutedM2ShadowRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new M1LayoutedM2ShadowRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  @Override
  public GavCalculator getGavCalculator() {
    return getM1GavCalculator();
  }

  @Override
  public ContentClass getRepositoryContentClass() {
    return contentClass;
  }

  @Override
  public ContentClass getMasterRepositoryContentClass() {
    return masterContentClass;
  }

  @Override
  protected Configurator getConfigurator() {
    return m1LayoutedM2ShadowRepositoryConfigurator;
  }

  @Override
  protected List<String> transformMaster2Shadow(final String path) {
    return transformM2toM1(path, null);
  }

  @Override
  protected List<String> transformShadow2Master(final String path) {
    return transformM1toM2(path);
  }

  @Override
  public boolean isMavenMetadataPath(final String path) {
    return M1ArtifactRecognizer.isMetadata(path);
  }
}

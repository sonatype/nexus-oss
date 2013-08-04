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

package org.sonatype.nexus.proxy.maven.maven2;

import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.maven.LayoutConverterShadowRepository;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.maven.gav.M2ArtifactRecognizer;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * A shadow repository that transforms M1 content hierarchy of master to M2 layouted shadow.
 *
 * @author cstamas
 */
@Component(role = ShadowRepository.class, hint = M2LayoutedM1ShadowRepository.ID, instantiationStrategy = "per-lookup",
    description = "Maven1 to Maven2")
public class M2LayoutedM1ShadowRepository
    extends LayoutConverterShadowRepository
{
  /**
   * This "mimics" the @Named("m1-m2-shadow")
   */
  public static final String ID = "m1-m2-shadow";

  @Requirement(hint = Maven2ContentClass.ID)
  private ContentClass contentClass;

  @Requirement(hint = Maven1ContentClass.ID)
  private ContentClass masterContentClass;

  @Requirement
  private M2LayoutedM1ShadowRepositoryConfigurator m2LayoutedM1ShadowRepositoryConfigurator;

  @Override
  protected M2LayoutedM1ShadowRepositoryConfiguration getExternalConfiguration(boolean forWrite) {
    return (M2LayoutedM1ShadowRepositoryConfiguration) super.getExternalConfiguration(forWrite);
  }

  @Override
  protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory() {
    return new CRepositoryExternalConfigurationHolderFactory<M2LayoutedM1ShadowRepositoryConfiguration>()
    {
      public M2LayoutedM1ShadowRepositoryConfiguration createExternalConfigurationHolder(CRepository config) {
        return new M2LayoutedM1ShadowRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
      }
    };
  }

  @Override
  public GavCalculator getGavCalculator() {
    return getM2GavCalculator();
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
    return m2LayoutedM1ShadowRepositoryConfigurator;
  }

  @Override
  protected List<String> transformMaster2Shadow(final String path) {
    return transformM1toM2(path);
  }

  @Override
  protected List<String> transformShadow2Master(final String path) {
    return transformM2toM1(path, Collections.singletonList("ejbs"));
  }

  @Override
  public boolean isMavenMetadataPath(final String path) {
    return M2ArtifactRecognizer.isMetadata(path);
  }
}

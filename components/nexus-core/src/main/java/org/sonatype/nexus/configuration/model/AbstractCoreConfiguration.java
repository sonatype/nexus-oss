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

package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractCoreConfiguration
    extends AbstractRevertableConfiguration
    implements CoreConfiguration
{
  private ApplicationConfiguration applicationConfiguration;

  private ExternalConfiguration<?> externalConfiguration;

  public AbstractCoreConfiguration(ApplicationConfiguration applicationConfiguration) {
    final Object extracted = extractConfiguration(applicationConfiguration.getConfigurationModel());

    if (extracted != null) {
      setOriginalConfiguration(extracted);
    }
    else {
      setOriginalConfiguration(getDefaultConfiguration());
    }

    this.applicationConfiguration = applicationConfiguration;
  }

  protected ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  protected ExternalConfiguration<?> prepareExternalConfiguration(Object configuration) {
    // usually nothing, but CRepository and CPlugin does have them
    return null;
  }

  public ExternalConfiguration<?> getExternalConfiguration() {
    if (externalConfiguration == null) {
      externalConfiguration = prepareExternalConfiguration(getOriginalConfiguration());
    }

    return externalConfiguration;
  }

  public Object getDefaultConfiguration() {
    return null;
  }

  @Override
  public boolean isDirty() {
    return isThisDirty() || (getExternalConfiguration() != null && getExternalConfiguration().isDirty());
  }

  @Override
  public void validateChanges()
      throws ConfigurationException
  {
    super.validateChanges();

    if (getExternalConfiguration() != null) {
      getExternalConfiguration().validateChanges();
    }
  }

  @Override
  public void commitChanges()
      throws ConfigurationException
  {
    super.commitChanges();

    if (getExternalConfiguration() != null) {
      getExternalConfiguration().commitChanges();
    }
  }

  @Override
  public void rollbackChanges() {
    super.rollbackChanges();

    if (getExternalConfiguration() != null) {
      getExternalConfiguration().rollbackChanges();
    }
  }

  // ==

  protected abstract Object extractConfiguration(Configuration configuration);
}

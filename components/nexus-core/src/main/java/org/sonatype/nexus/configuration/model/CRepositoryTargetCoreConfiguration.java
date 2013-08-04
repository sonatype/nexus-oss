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

import java.util.List;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CRepositoryTargetCoreConfiguration
    extends AbstractCoreConfiguration
{
  public CRepositoryTargetCoreConfiguration(ApplicationConfiguration configuration) {
    super(configuration);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<CRepositoryTarget> getConfiguration(boolean forWrite) {
    return (List<CRepositoryTarget>) super.getConfiguration(forWrite);
  }

  @Override
  protected List<CRepositoryTarget> extractConfiguration(Configuration configuration) {
    return configuration.getRepositoryTargets();
  }

  @Override
  public ValidationResponse doValidateChanges(Object changedConfiguration) {
    return new ValidationResponse();
  }
}

/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.validation.ValidationMessage;
import org.sonatype.nexus.validation.ValidationResponse;
import org.sonatype.nexus.validation.ValidationResponseException;

public abstract class AbstractShadowRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{

  @Override
  public void doApplyConfiguration(Repository repository,
                                   ApplicationConfiguration configuration,
                                   CRepositoryCoreConfiguration coreConfig)
  {
    // Shadows are read only
    repository.setWritePolicy(RepositoryWritePolicy.READ_ONLY);

    super.doApplyConfiguration(repository, configuration, coreConfig);

    ShadowRepository shadowRepository = repository.adaptToFacet(ShadowRepository.class);

    AbstractShadowRepositoryConfiguration extConf =
        (AbstractShadowRepositoryConfiguration) coreConfig.getExternalConfiguration().getConfiguration(false);

    try {
      shadowRepository.setMasterRepository(getRepositoryRegistry().getRepository(extConf.getMasterRepositoryId()));
    }
    catch (IncompatibleMasterRepositoryException | NoSuchRepositoryException e) {
      ValidationMessage message = new ValidationMessage("shadowOf", e.getMessage());
      ValidationResponse response = new ApplicationValidationResponse();
      response.addError(message);
      throw new ValidationResponseException(response);
    }
  }
}

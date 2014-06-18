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
package org.sonatype.nexus.plugins.siesta.internal;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.siesta.ValidationErrorXO;
import org.sonatype.siesta.server.validation.ValidationExceptionMapperSupport;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Maps {@link InvalidConfigurationException} to 400 with a list of {@link ValidationErrorXO} as body.
 *
 * @since 2.4
 */
@Named
@Singleton
public class InvalidConfigurationExceptionMapper
    extends ValidationExceptionMapperSupport<InvalidConfigurationException>
{
  @Override
  protected List<ValidationErrorXO> getValidationErrors(final InvalidConfigurationException exception) {
    ValidationResponse response = exception.getValidationResponse();
    if (response != null) {
      List<ValidationMessage> errors = response.getValidationErrors();
      if (errors != null && !errors.isEmpty()) {
        return Lists.transform(errors, new Function<ValidationMessage, ValidationErrorXO>()
        {
          @Nullable
          @Override
          public ValidationErrorXO apply(@Nullable final ValidationMessage message) {
            if (message != null) {
              return new ValidationErrorXO(message.getKey(), message.getMessage());
            }
            return null;
          }
        });
      }
    }

    return Lists.newArrayList(new ValidationErrorXO(exception.getMessage()));
  }
}
